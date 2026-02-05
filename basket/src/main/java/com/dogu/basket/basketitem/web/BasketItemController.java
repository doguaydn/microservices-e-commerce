package com.dogu.basket.basketitem.web;

import com.dogu.basket.basketitem.api.BasketItemDto;
import com.dogu.basket.basketitem.api.BasketItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/basket-items")
public class BasketItemController {
    @Autowired
    BasketItemService service;

    @PostMapping
    public BasketItemResponse save(@RequestBody BasketItemRequest info) {
        BasketItemDto basketItemDto = service.save(toDto(info));
        return toResponse(basketItemDto);
    }

    @GetMapping("/{id}")
    public BasketItemResponse get(@PathVariable int id) {
        BasketItemDto basketItemDto = service.get(id);
        return toResponse(basketItemDto);
    }

    @GetMapping
    public List<BasketItemResponse> getAll() {
        List<BasketItemDto> basketItems = service.getAll();
        return basketItems.stream().map(this::toResponse).toList();
    }

    @GetMapping("/user/{userId}")
    public List<BasketItemResponse> getByUserId(@PathVariable int userId) {
        List<BasketItemDto> basketItems = service.getByUserId(userId);
        return basketItems.stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}")
    public BasketItemResponse update(@PathVariable int id, @RequestBody BasketItemRequest info) {
        BasketItemDto dto = toDto(info);
        dto.setId(id);
        BasketItemDto basketItemDto = service.update(dto);
        return toResponse(basketItemDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }

    public BasketItemResponse toResponse(BasketItemDto dto) {
        BasketItemResponse response = new BasketItemResponse();
        response.id = dto.getId();
        response.userId = dto.getUserId();
        response.productId = dto.getProductId();
        response.quantity = dto.getQuantity();
        return response;
    }

    public BasketItemDto toDto(BasketItemRequest request) {
        BasketItemDto dto = new BasketItemDto();
        dto.setUserId(request.userId);
        dto.setProductId(request.productId);
        dto.setQuantity(request.quantity);
        return dto;
    }
}
