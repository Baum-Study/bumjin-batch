package com.example.batch.jobs.jdbcpagingitemreader;

import com.example.batch.common.Customer;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class CustomerItemSqlParameterSourceProvider implements ItemSqlParameterSourceProvider<Customer> {
  @Override
  public SqlParameterSource createSqlParameterSource(Customer item) {
    return new BeanPropertySqlParameterSource(item);
  }
}