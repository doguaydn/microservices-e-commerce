package com.dogu.basket.order.api;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(String orderId, int userId, String itemsJson, double totalAmount);
    OrderDto getByOrderId(String orderId);
    List<OrderDto> getByUserId(int userId);
    List<OrderDto> getAll();
    OrderDto updateStatus(String orderId, String status);
    OrderDto cancelOrder(String orderId);
    List<OrderDto> getByStatus(String status);
    long countByStatus(String status);
}
