package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * - @EnableBatchProcessing  Spring Batch를 사용하기 위한 설정을 추가한다. Spring 3.0v 이후는 불필요
 */
@SpringBootApplication
public class BatchApplication {

  public static void main(String[] args) {
    SpringApplication.run(BatchApplication.class, args);
  }

}
