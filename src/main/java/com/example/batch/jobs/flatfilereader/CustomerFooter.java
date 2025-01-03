package com.example.batch.jobs.flatfilereader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileFooterCallback;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomerFooter implements FlatFileFooterCallback {
  ConcurrentHashMap<String, Integer> aggregateCustomers;

  public CustomerFooter(ConcurrentHashMap<String, Integer> aggregateCustomers) {
    this.aggregateCustomers = aggregateCustomers;
  }

  @Override
  public void writeFooter(Writer writer) throws IOException {
    writer.write("총 고객 수: " + aggregateCustomers.get("TOTAL_CUSTOMERS"));
    writer.write(System.lineSeparator());
    writer.write("총 나이: " + aggregateCustomers.get("TOTAL_AGES"));
  }
}