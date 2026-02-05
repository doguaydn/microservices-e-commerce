package com.dogu.basket.basketitem.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem, Integer> {
    List<BasketItem> findByUserId(int userId);
}
