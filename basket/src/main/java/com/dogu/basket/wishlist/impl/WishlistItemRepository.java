package com.dogu.basket.wishlist.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Integer> {
    List<WishlistItem> findByUserId(int userId);
    Optional<WishlistItem> findByUserIdAndProductId(int userId, int productId);
}
