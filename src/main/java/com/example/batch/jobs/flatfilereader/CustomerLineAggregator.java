package com.example.batch.jobs.flatfilereader;

import com.example.batch.common.CustomerDto;
import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomerLineAggregator implements LineAggregator<CustomerDto> {
  @Override
  public String aggregate(CustomerDto item) {
    return item.name() + "," + item.age();
  }
}