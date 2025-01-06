package com.example.batch.jobs.mission02.jobs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "MEMBER_DELETE_JOB")
@RequiredArgsConstructor
public class MemberDeleteJobConfig {

  private final DataSource dataSource;
  private final EntityManagerFactory entityManagerFactory;

  @Bean
  public JpaPagingItemReader<Long> inactiveMemberReader() {
    return new JpaPagingItemReaderBuilder<Long>()
        .name("inActiveMemberReader")
        .queryString("SELECT id FROM Member m WHERE m.status = 'INACTIVE' ORDER BY m.id DESC")
        .pageSize(1000)
        .entityManagerFactory(entityManagerFactory)
        .build();
  }

  @Bean
  public ItemWriter<Long> memberDeleteWriter() {
    return chunk -> {
      List<Long> ids = chunk.getItems().stream()
          .map(Long.class::cast)
          .toList();
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();

      entityManager.createQuery("DELETE FROM Image i WHERE i.member.id IN :ids")
          .setParameter("ids", ids)
          .executeUpdate();

      entityManager.createQuery("DELETE FROM Member m WHERE m.id IN :ids")
          .setParameter("ids", ids)
          .executeUpdate();

      entityManager.getTransaction().commit();
      entityManager.close();
    };
  }

  @Bean
  public Step memberDeleteStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("memberDeleteStep", jobRepository)
        .<Long, Long>chunk(1000, transactionManager) // 청크 크기 설정
        .reader(inactiveMemberReader())
        .writer(memberDeleteWriter())
        .build();
  }

  @Bean
  public Job memberDeleteJob(JobRepository jobRepository, Step memberDeleteStep) {
    log.info("------------------ Init MemberDeleteJob -----------------");
    return new JobBuilder("MEMBER_DELETE_JOB", jobRepository)
        .start(memberDeleteStep)
        .build();
  }
}
