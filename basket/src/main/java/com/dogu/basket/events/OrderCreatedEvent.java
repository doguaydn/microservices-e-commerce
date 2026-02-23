package com.dogu.basket.events;

import java.time.LocalDateTime;
import java.util.List;

public class OrderCreatedEvent {
    private String orderId;
    private int userId;
    private String email;
    private List<OrderItemEvent> items;
    private double totalAmount;
    private LocalDateTime timestamp;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String orderId, int userId, String email, List<OrderItemEvent> items, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.email = email;
        this.items = items;
        this.totalAmount = totalAmount;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrderItemEvent> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEvent> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
