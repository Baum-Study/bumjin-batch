package com.example.batch.jobs.mission01.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySettlement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private long sellerId;
  private String settlementMonth;
  private BigDecimal totalAmount;
}
