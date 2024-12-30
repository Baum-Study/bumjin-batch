package com.example.batch.jobs.mybatis;

import com.example.batch.common.Customer;
import org.springframework.batch.item.ItemProcessor;

public class LowerCaseItemProcessor implements ItemProcessor<Customer, Customer> {
  @Override
  public Customer process(Customer item) throws Exception {
    item.setName(item.getName().toLowerCase());
    item.setGender(item.getGender().toLowerCase());
    return item;
  }
}