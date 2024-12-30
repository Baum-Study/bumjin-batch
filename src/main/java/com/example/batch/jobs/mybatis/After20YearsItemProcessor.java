package com.example.batch.jobs.mybatis;

import com.example.batch.common.Customer;
import org.springframework.batch.item.ItemProcessor;

public class After20YearsItemProcessor implements ItemProcessor<Customer, Customer> {
  @Override
  public Customer process(Customer item) throws Exception {
    item.setAge(item.getAge() + 20);
    return item;
  }
}