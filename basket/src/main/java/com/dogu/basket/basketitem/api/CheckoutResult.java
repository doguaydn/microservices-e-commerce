package com.dogu.basket.basketitem.api;

import java.util.List;

public class CheckoutResult {
    private String orderId;
    private int userId;
    private List<BasketItemDto> items;
    private double totalAmount;

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

    public List<BasketItemDto> getItems() {
        return items;
    }

    public void setItems(List<BasketItemDto> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
