package com.example.batch.jobs.jdbcpagingitemreader;

import com.example.batch.common.CustomerDto;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class CustomerItemSqlParameterSourceProvider implements ItemSqlParameterSourceProvider<CustomerDto> {
  @Override
  public SqlParameterSource createSqlParameterSource(CustomerDto item) {
    return new BeanPropertySqlParameterSource(item);
  }
}