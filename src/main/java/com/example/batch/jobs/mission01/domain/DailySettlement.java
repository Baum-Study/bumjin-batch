package com.example.batch.jobs.mission01.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySettlement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private long sellerId;
  private LocalDate settlementDate;
  private BigDecimal totalAmount;

  public static DailySettlement addAmount(DailySettlement settlement, BigDecimal amount) {
    return DailySettlement.builder()
        .id(settlement.getId())
        .sellerId(settlement.getSellerId())
        .settlementDate(settlement.getSettlementDate())
        .totalAmount(settlement.getTotalAmount().add(amount))
        .build();
  }
}
