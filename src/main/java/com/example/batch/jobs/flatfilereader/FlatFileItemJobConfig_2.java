package com.example.batch.jobs.flatfilereader;

import com.example.batch.common.CustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "FLAT_FILE_WRITER_CHUNK_JOB")
public class FlatFileItemJobConfig_2 {

  /**
   * CHUNK 크기를 지정한다.
   */
  public static final int CHUNK_SIZE = 100;
  public static final String ENCODING = "UTF-8";
  public static final String FLAT_FILE_WRITER_CHUNK_JOB = "FLAT_FILE_WRITER_CHUNK_JOB";

  private final ConcurrentHashMap<String, Integer> aggregateInfos = new ConcurrentHashMap<>();

  private final ItemProcessor<CustomerDto, CustomerDto> itemProcessor = new AggregateCustomerProcessor(aggregateInfos);

  @Bean
  public FlatFileItemReader<CustomerDto> flatFileItemReader() {

    return new FlatFileItemReaderBuilder<CustomerDto>()
        .name("FlatFileItemReader")
        .resource(new ClassPathResource("./customer.csv"))
        .encoding(ENCODING)
        .delimited().delimiter(",")
        .names("name", "age", "gender")
        .targetType(CustomerDto.class)
        .build();
  }

  @Bean
  public FlatFileItemWriter<CustomerDto> flatFileItemWriter() {

    return new FlatFileItemWriterBuilder<CustomerDto>()
        .name("flatFileItemWriter")
        .resource(new FileSystemResource("./output/customer-output-new-2.csv"))
        .encoding(ENCODING)
        .delimited().delimiter("\t")
        .names("Name", "Age", "Gender")
        .append(false)
        .lineAggregator(new CustomerLineAggregator())
        .headerCallback(new CustomerHeader())
        .footerCallback(new CustomerFooter(aggregateInfos))
        .build();
  }


  @Bean
  public Step flatFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    log.info("------------------ Init flatFileStep -----------------");

    return new StepBuilder("flatFileStep", jobRepository)
        .<CustomerDto, CustomerDto>chunk(CHUNK_SIZE, transactionManager)
        .reader(flatFileItemReader())
        .processor(itemProcessor)
        .writer(flatFileItemWriter())
        .build();
  }

  @Bean
  public Job flatFileJob(Step flatFileStep, JobRepository jobRepository) {
    log.info("------------------ Init flatFileJob -----------------");
    return new JobBuilder(FLAT_FILE_WRITER_CHUNK_JOB, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(flatFileStep)
        .build();
  }
}