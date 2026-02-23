package com.dogu.basket.order.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByUserId(int userId);
    List<Order> findByStatus(String status);
    long countByStatus(String status);
}
