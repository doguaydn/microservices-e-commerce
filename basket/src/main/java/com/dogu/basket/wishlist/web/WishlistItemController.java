package com.dogu.basket.wishlist.web;

import com.dogu.basket.wishlist.api.WishlistItemDto;
import com.dogu.basket.wishlist.api.WishlistItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistItemController {

    @Autowired
    WishlistItemService wishlistItemService;

    @PostMapping
    public WishlistItemResponse save(@RequestBody WishlistItemRequest request) {
        WishlistItemDto dto = wishlistItemService.save(request.userId, request.productId);
        return toResponse(dto);
    }

    @GetMapping("/user/{userId}")
    public List<WishlistItemResponse> getByUserId(@PathVariable int userId) {
        return wishlistItemService.getByUserId(userId).stream().map(this::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        wishlistItemService.delete(id);
    }

    private WishlistItemResponse toResponse(WishlistItemDto dto) {
        WishlistItemResponse response = new WishlistItemResponse();
        response.id = dto.getId();
        response.userId = dto.getUserId();
        response.productId = dto.getProductId();
        response.createdAt = dto.getCreatedAt();
        return response;
    }
}
