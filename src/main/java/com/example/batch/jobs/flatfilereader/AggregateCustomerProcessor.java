package com.example.batch.jobs.flatfilereader;

import com.example.batch.common.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AggregateCustomerProcessor implements ItemProcessor<Customer, Customer> {

  ConcurrentHashMap<String, Integer> aggregateCustomers;

  public AggregateCustomerProcessor(ConcurrentHashMap<String, Integer> aggregateCustomers) {
    this.aggregateCustomers = aggregateCustomers;
  }

  @Override
  public Customer process(Customer item) throws Exception {
    aggregateCustomers.putIfAbsent("TOTAL_CUSTOMERS", 0);
    aggregateCustomers.putIfAbsent("TOTAL_AGES", 0);

    aggregateCustomers.put("TOTAL_CUSTOMERS", aggregateCustomers.get("TOTAL_CUSTOMERS") + 1);
    aggregateCustomers.put("TOTAL_AGES", aggregateCustomers.get("TOTAL_AGES") + item.age());
    return item;
  }
}