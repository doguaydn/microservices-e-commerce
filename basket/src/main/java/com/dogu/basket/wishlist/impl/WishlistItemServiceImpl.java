package com.dogu.basket.wishlist.impl;

import com.dogu.basket.wishlist.api.WishlistItemDto;
import com.dogu.basket.wishlist.api.WishlistItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistItemServiceImpl implements WishlistItemService {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Override
    @CacheEvict(value = "wishlist-by-user", key = "#userId")
    public WishlistItemDto save(int userId, int productId) {
        // Aynı ürün zaten favorilerde mi kontrol et
        if (wishlistItemRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new RuntimeException("Product already in wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setCreatedAt(LocalDateTime.now());
        wishlistItemRepository.save(item);
        return toDto(item);
    }

    @Override
    @Cacheable(value = "wishlist-by-user", key = "#userId")
    public List<WishlistItemDto> getByUserId(int userId) {
        return wishlistItemRepository.findByUserId(userId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @CacheEvict(value = "wishlist-by-user", allEntries = true)
    public void delete(int id) {
        WishlistItem item = wishlistItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));
        wishlistItemRepository.delete(item);
    }

    private WishlistItemDto toDto(WishlistItem entity) {
        WishlistItemDto dto = new WishlistItemDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setProductId(entity.getProductId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
