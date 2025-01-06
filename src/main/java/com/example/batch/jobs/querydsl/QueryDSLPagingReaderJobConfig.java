package com.example.batch.jobs.querydsl;

import com.example.batch.common.Customer;
import com.example.batch.common.QCustomer;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "QUERYDSL_PAGING_READER_JOB")
@RequiredArgsConstructor
public class QueryDSLPagingReaderJobConfig {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String QUERYDSL_PAGING_READER_JOB = "QUERYDSL_PAGING_READER_JOB";

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public QuerydslPagingItemReader<Customer> customerQuerydslPagingItemReader() {
        return new QuerydslPagingItemReaderBuilder<Customer>()
                .name("customerQuerydslPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .chunkSize(2)
                .querySupplier(jpaQueryFactory -> jpaQueryFactory.select(QCustomer.customer).from(QCustomer.customer).where(QCustomer.customer.age.gt(20)))
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> customerQuerydslFlatFileItemWriter() {

        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerQuerydslFlatFileItemWriter")
                .resource(new FileSystemResource("./output/customer-output-new-7.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name", "Age", "Gender")
                .build();
    }


    @Bean
    public Step customerQuerydslPagingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        log.info("------------------ Init customerQuerydslPagingStep -----------------");

        return new StepBuilder("customerJpaPagingStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(customerQuerydslPagingItemReader())
                .processor(new CustomerItemProcessor())
                .writer(customerQuerydslFlatFileItemWriter())
                .build();
    }

    @Bean
    public Job customerJpaPagingJob(Step customerJdbcPagingStep, JobRepository jobRepository) {
        log.info("------------------ Init customerJpaPagingJob -----------------");
        return new JobBuilder(QUERYDSL_PAGING_READER_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerJdbcPagingStep)
                .build();
    }
}