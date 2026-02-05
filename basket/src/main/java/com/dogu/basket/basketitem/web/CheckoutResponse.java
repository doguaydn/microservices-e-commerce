package com.dogu.basket.basketitem.web;

import java.util.List;

public class CheckoutResponse {
    public String orderId;
    public int userId;
    public List<BasketItemResponse> items;
    public double totalAmount;
    public String message;
}
