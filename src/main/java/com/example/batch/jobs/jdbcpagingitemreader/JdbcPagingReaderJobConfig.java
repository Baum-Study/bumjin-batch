package com.example.batch.jobs.jdbcpagingitemreader;

import com.example.batch.common.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "JDBC_PAGING_CHUNK_JOB")
@RequiredArgsConstructor
public class JdbcPagingReaderJobConfig {

  /**
   * CHUNK 크기를 지정
   */
  public static final int CHUNK_SIZE = 2;
  public static final String ENCODING = "UTF-8";
  public static final String JDBC_PAGING_CHUNK_JOB = "JDBC_PAGING_CHUNK_JOB";

  private final DataSource dataSource;

  private RowMapper<Customer> customerRowMapper() {
    return (rs, rowNum) -> new Customer(
        rs.getString("name"),
        rs.getInt("age"),
        rs.getString("gender")
    );
  }

  @Bean
  public JdbcPagingItemReader<Customer> jdbcPagingItemReader() throws Exception {
    Map<String, Object> parameterValue = new HashMap<>();
    parameterValue.put("age", 10);

    return new JdbcPagingItemReaderBuilder<Customer>()
        .name("jdbcPagingItemReader")
        .fetchSize(CHUNK_SIZE)
        .dataSource(dataSource)
        .rowMapper(customerRowMapper())
        .queryProvider(queryProvider())
        .parameterValues(parameterValue)
        .build();
  }

  @Bean
  public PagingQueryProvider queryProvider() throws Exception {
    SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
    queryProvider.setDataSource(dataSource);
    queryProvider.setSelectClause("id, name, age, gender");
    queryProvider.setFromClause("from customer");
    queryProvider.setWhereClause("where age >= :age");

    Map<String, Order> sortKeys = new HashMap<>(1);
    sortKeys.put("id", Order.DESCENDING);

    queryProvider.setSortKeys(sortKeys);

    return queryProvider.getObject();
  }

  @Bean
  public FlatFileItemWriter<Customer> customerFlatFileItemWriter() {
    return new FlatFileItemWriterBuilder<Customer>()
        .name("customerFlatFileItemWriter")
        .resource(new FileSystemResource("./output/customer-output-new-3.csv"))
        .encoding(ENCODING)
        .delimited().delimiter("\t")
        .names("Name", "Age", "Gender")
        .build();
  }


  @Bean
  public Step customerJdbcPagingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
    log.info("------------------ Init customerJdbcPagingStep -----------------");

    return new StepBuilder("customerJdbcPagingStep", jobRepository)
        .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
        .reader(jdbcPagingItemReader())
        .writer(customerFlatFileItemWriter())
        .build();
  }

  @Bean
  public Job customerJdbcPagingJob(Step customerJdbcPagingStep, JobRepository jobRepository) {
    log.info("------------------ Init customerJdbcPagingJob -----------------");
    return new JobBuilder(JDBC_PAGING_CHUNK_JOB, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(customerJdbcPagingStep)
        .build();
  }
}