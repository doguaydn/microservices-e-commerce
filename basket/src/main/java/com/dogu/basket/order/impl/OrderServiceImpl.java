package com.dogu.basket.order.impl;

import com.dogu.basket.order.api.OrderDto;
import com.dogu.basket.order.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "orders-by-user", allEntries = true),
            @CacheEvict(value = "orders-all", allEntries = true)
    })
    public OrderDto createOrder(String orderId, int userId, String itemsJson, double totalAmount) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setItems(itemsJson);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderDto getByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toDto(order);
    }

    @Override
    @Cacheable(value = "orders-by-user", key = "#userId")
    public List<OrderDto> getByUserId(int userId) {
        return orderRepository.findByUserId(userId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Cacheable(value = "orders-all")
    public List<OrderDto> getAll() {
        return orderRepository.findAll()
                .stream().map(this::toDto).toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "orders", key = "#orderId"),
            @CacheEvict(value = "orders-by-user", allEntries = true),
            @CacheEvict(value = "orders-all", allEntries = true)
    })
    public OrderDto updateStatus(String orderId, String status) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "orders", key = "#orderId"),
            @CacheEvict(value = "orders-by-user", allEntries = true),
            @CacheEvict(value = "orders-all", allEntries = true)
    })
    public OrderDto cancelOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if ("DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Delivered orders cannot be cancelled");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return toDto(order);
    }

    @Override
    public List<OrderDto> getByStatus(String status) {
        return orderRepository.findByStatus(status)
                .stream().map(this::toDto).toList();
    }

    @Override
    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    private OrderDto toDto(Order entity) {
        OrderDto dto = new OrderDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setUserId(entity.getUserId());
        dto.setItems(entity.getItems());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
