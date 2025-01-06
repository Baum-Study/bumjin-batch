package com.example.batch.jobs.mission01.jobs.settlement;

import com.example.batch.jobs.mission01.domain.MonthlySettlement;
import com.example.batch.jobs.mission01.domain.Payment;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "MONTHLY_SETTLEMENT_JOB")
@RequiredArgsConstructor
public class MonthlySettlementJobConfig {

  private final DataSource dataSource;

  @Bean
  public FlatFileItemReader<Payment> flatFileItemReader() {

    return new FlatFileItemReaderBuilder<Payment>()
        .name("PaymentReader")
        .resource(new ClassPathResource("./payment.csv"))
        .encoding("UTF-8")
        .delimited()
        .delimiter(",")
        .names("sellerId", "price", "paymentDate")
        .fieldSetMapper(fieldSet -> Payment.builder()
            .sellerId(fieldSet.readLong("sellerId"))
            .price(fieldSet.readBigDecimal("price"))
            .paymentDate(LocalDateTime.parse(
                fieldSet.readString("paymentDate"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            ))
            .build())
        .build();
  }

  @Bean
  public JdbcBatchItemWriter<MonthlySettlement> writer() {
    return new JdbcBatchItemWriterBuilder<MonthlySettlement>()
        .dataSource(dataSource)
        .sql("INSERT INTO monthly_settlement (seller_id, settlement_month, total_amount) " +
            "VALUES (:sellerId, :settlementMonth, :totalAmount) " +
            "ON DUPLICATE KEY UPDATE total_amount = total_amount + VALUES(total_amount)")
        .beanMapped()
        .build();
  }

  @Bean
  public Step monthlySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("monthlySettlementStep", jobRepository)
        .<Payment, MonthlySettlement>chunk(1000, transactionManager)
        .reader(flatFileItemReader())
        .processor(item ->
            MonthlySettlement.builder()
                .sellerId(item.getSellerId())
                .settlementMonth(YearMonth.from(item.getPaymentDate()).toString())
                .totalAmount(item.getPrice())
                .build())
        .writer(writer())
        .build();
  }

  @Bean
  public Job flatFileJob(Step monthlySettlementStep, JobRepository jobRepository) {
    log.info("------------------ Init flatFileJob -----------------");
    return new JobBuilder("MONTHLY_SETTLEMENT_JOB", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(monthlySettlementStep)
        .build();
  }
}