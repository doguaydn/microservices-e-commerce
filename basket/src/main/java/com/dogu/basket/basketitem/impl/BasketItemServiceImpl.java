package com.dogu.basket.basketitem.impl;

import com.dogu.basket.basketitem.api.BasketItemDto;
import com.dogu.basket.basketitem.api.BasketItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BasketItemServiceImpl implements BasketItemService {

    @Autowired
    BasketItemRepository basketItemRepository;

    @Autowired
    RestTemplate restTemplate;

    private static final String STOCK_SERVICE_URL = "http://localhost:9093";
    private static final String AUTH_SERVICE_URL = "http://localhost:9092";

    @Override
    public BasketItemDto save(BasketItemDto param) {
        validateUserExists(param.getUserId());
        validateProductExists(param.getProductId());
        checkProductStock(param.getProductId(), param.getQuantity());

        BasketItem basketItem = toEntity(param, null);
        basketItemRepository.save(basketItem);
        return toDto(basketItem);
    }

    @Override
    public BasketItemDto get(int id) {
        BasketItem entity = basketItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Basket item not found"));
        return toDto(entity);
    }

    @Override
    public List<BasketItemDto> getAll() {
        List<BasketItem> basketItems = basketItemRepository.findAll();
        return basketItems.stream().map(this::toDto).toList();
    }

    @Override
    public List<BasketItemDto> getByUserId(int userId) {
        validateUserExists(userId);
        List<BasketItem> basketItems = basketItemRepository.findByUserId(userId);
        return basketItems.stream().map(this::toDto).toList();
    }

    @Override
    public BasketItemDto update(BasketItemDto info) {
        validateProductExists(info.getProductId());
        checkProductStock(info.getProductId(), info.getQuantity());

        BasketItem basketItem = basketItemRepository.findById(info.getId())
                .orElseThrow(() -> new RuntimeException("Basket item not found"));
        basketItem = toEntity(info, basketItem);
        return toDto(basketItemRepository.save(basketItem));
    }

    @Override
    public void delete(int id) {
        BasketItem entity = basketItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Basket item not found"));
        basketItemRepository.delete(entity);
    }

    private void validateUserExists(int userId) {
        try {
            restTemplate.getForEntity(AUTH_SERVICE_URL + "/users/" + userId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("User not found with id: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Auth service is not available");
        }
    }

    private void validateProductExists(int productId) {
        try {
            restTemplate.getForEntity(STOCK_SERVICE_URL + "/products/" + productId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Product not found with id: " + productId);
        } catch (Exception e) {
            throw new RuntimeException("Stock service is not available");
        }
    }

    private void checkProductStock(int productId, int requestedQuantity) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    STOCK_SERVICE_URL + "/products/" + productId, Map.class);
            Map<String, Object> product = response.getBody();
            if (product != null) {
                int availableQuantity = (int) product.get("quantity");
                if (availableQuantity < requestedQuantity) {
                    throw new RuntimeException("Insufficient stock. Available: " + availableQuantity + ", Requested: " + requestedQuantity);
                }
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
    }

    private BasketItem toEntity(BasketItemDto request, BasketItem entity) {
        if (entity == null) {
            entity = new BasketItem();
        }
        entity.setUserId(request.getUserId());
        entity.setProductId(request.getProductId());
        entity.setQuantity(request.getQuantity());
        return entity;
    }

    private BasketItemDto toDto(BasketItem entity) {
        BasketItemDto dto = new BasketItemDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setProductId(entity.getProductId());
        dto.setQuantity(entity.getQuantity());
        return dto;
    }
}
