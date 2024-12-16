package com.example.batch.jobs.jdbcpagingitemreader;

import com.example.batch.common.CustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "JDBC_BATCH_WRITER_CHUNK_JOB")
@RequiredArgsConstructor
public class JdbcBatchItemJobConfig {

  /**
   * CHUNK 크기를 지정
   */
  public static final int CHUNK_SIZE = 100;
  public static final String ENCODING = "UTF-8";
  public static final String JDBC_BATCH_WRITER_CHUNK_JOB = "JDBC_BATCH_WRITER_CHUNK_JOB";

  private final DataSource dataSource;

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
  public JdbcBatchItemWriter<CustomerDto> flatFileItemWriter() {

    return new JdbcBatchItemWriterBuilder<CustomerDto>()
        .dataSource(dataSource)
        .sql("INSERT INTO customer2 (name, age, gender) VALUES (:name, :age, :gender)")
        .itemSqlParameterSourceProvider(new CustomerItemSqlParameterSourceProvider())
        .build();
  }


  @Bean
  public Step flatFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    log.info("------------------ Init flatFileStep -----------------");

    return new StepBuilder("flatFileStep", jobRepository)
        .<CustomerDto, CustomerDto>chunk(CHUNK_SIZE, transactionManager)
        .reader(flatFileItemReader())
        .writer(flatFileItemWriter())
        .build();
  }

  @Bean
  public Job flatFileJob(Step flatFileStep, JobRepository jobRepository) {
    log.info("------------------ Init flatFileJob -----------------");
    return new JobBuilder(JDBC_BATCH_WRITER_CHUNK_JOB, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(flatFileStep)
        .build();
  }
}