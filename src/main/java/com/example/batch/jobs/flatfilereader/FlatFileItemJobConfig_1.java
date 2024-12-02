package com.example.batch.jobs.flatfilereader;

import com.example.batch.common.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "FLAT_FILE_CHUNK_JOB")
public class FlatFileItemJobConfig_1 {

  /**
   * CHUNK 크기 지정
   */
  public static final int CHUNK_SIZE = 10;
  public static final String ENCODING = "UTF-8";
  public static final String FLAT_FILE_CHUNK_JOB = "FLAT_FILE_CHUNK_JOB";

  @Bean
  public FlatFileItemReader<Customer> flatFileItemReader1() {
    return new FlatFileItemReaderBuilder<Customer>()
        .name("flatFileItemReader1")
        .resource(new ClassPathResource("./customer.csv"))
        .encoding(ENCODING)
        .delimited().delimiter(",")
        .names("name", "age", "gender")
        .targetType(Customer.class)
        .build();
  }

  @Bean
  public FlatFileItemWriter<Customer> flatFileItemWriter1() {
    return new FlatFileItemWriterBuilder<Customer>()
        .name("flatFileItemWriter1")
        .resource(new FileSystemResource("./output/customer-output-new.csv"))
        .encoding(ENCODING)
        .delimited().delimiter("\t")
        .names("Name", "Age", "Gender")
        .build();
  }

  @Bean
  public Step flatFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    log.info("------------------ Init flatFileStep -----------------");

    return new StepBuilder("flatFileStep1", jobRepository)
        .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
        .reader(flatFileItemReader1())
        .writer(flatFileItemWriter1())
        .build();
  }

  @Bean
  public Job flatFileJob(Step flatFileStep, JobRepository jobRepository) {
    log.info("------------------ Init flatFileJob -----------------");
    return new JobBuilder(FLAT_FILE_CHUNK_JOB, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(flatFileStep)
        .build();
  }
}
