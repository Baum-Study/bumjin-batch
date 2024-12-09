package com.example.batch.common;

import lombok.Builder;

@Builder
public record Customer(
    String name,
    int age,
    String gender
) {
}
