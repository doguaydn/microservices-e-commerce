package com.dogu.basket.wishlist.api;

import java.util.List;

public interface WishlistItemService {
    WishlistItemDto save(int userId, int productId);
    List<WishlistItemDto> getByUserId(int userId);
    void delete(int id);
}
