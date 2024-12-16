package com.example.batch.common;

import lombok.Builder;

@Builder
public record CustomerDto(
    String name,
    int age,
    String gender
) {
}
