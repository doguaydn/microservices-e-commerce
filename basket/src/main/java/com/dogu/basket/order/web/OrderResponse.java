package com.dogu.basket.order.web;

import java.time.LocalDateTime;

public class OrderResponse {
    public int id;
    public String orderId;
    public int userId;
    public String items;
    public double totalAmount;
    public String status;
    public LocalDateTime createdAt;
}
