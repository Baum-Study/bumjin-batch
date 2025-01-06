package com.example.batch.jobs.mission03.domain;

import java.time.LocalDateTime;

public class Order {
    private long id;
    private long memberId;
    private long productId;
    private int quantity;
    private int totalPrice;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private OrderStatus orderStatus;
}
