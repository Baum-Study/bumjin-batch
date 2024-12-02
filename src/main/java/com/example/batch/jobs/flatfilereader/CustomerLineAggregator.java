package com.example.batch.jobs.flatfilereader;

import com.example.batch.common.Customer;
import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomerLineAggregator implements LineAggregator<Customer> {
  @Override
  public String aggregate(Customer item) {
    return item.name() + "," + item.age();
  }
}