package com.dogu.basket.basketitem.impl;

import com.dogu.basket.basketitem.api.BasketItemDto;
import com.dogu.basket.basketitem.api.BasketItemService;
import com.dogu.basket.basketitem.api.CheckoutResult;
import com.dogu.basket.events.OrderCreatedEvent;
import com.dogu.basket.events.OrderEventPublisher;
import com.dogu.basket.events.OrderItemEvent;
import com.dogu.basket.order.api.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BasketItemServiceImpl implements BasketItemService {

    @Autowired
    BasketItemRepository basketItemRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    OrderEventPublisher eventPublisher;

    @Autowired
    OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String STOCK_SERVICE_URL = "http://127.0.0.1:9093";
    private static final String AUTH_SERVICE_URL = "http://127.0.0.1:9092";

    @Override
    @Caching(evict = {
            @CacheEvict(value = "basket-by-user", key = "#param.userId"),
            @CacheEvict(value = "basket-items", allEntries = true)
    })
    public BasketItemDto save(BasketItemDto param) {
        validateUserExists(param.getUserId());
        validateProductExists(param.getProductId());
        checkProductStock(param.getProductId(), param.getQuantity());

        BasketItem basketItem = toEntity(param, null);
        basketItemRepository.save(basketItem);
        return toDto(basketItem);
    }

    @Override
    @Cacheable(value = "basket-items", key = "#id")
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
    @Cacheable(value = "basket-by-user", key = "#userId")
    public List<BasketItemDto> getByUserId(int userId) {
        validateUserExists(userId);
        List<BasketItem> basketItems = basketItemRepository.findByUserId(userId);
        return basketItems.stream().map(this::toDto).toList();
    }

    @Override
    @Caching(
            put = {@CachePut(value = "basket-items", key = "#info.id")},
            evict = {@CacheEvict(value = "basket-by-user", key = "#info.userId")}
    )
    public BasketItemDto update(BasketItemDto info) {
        validateProductExists(info.getProductId());
        checkProductStock(info.getProductId(), info.getQuantity());

        BasketItem basketItem = basketItemRepository.findById(info.getId())
                .orElseThrow(() -> new RuntimeException("Basket item not found"));
        basketItem = toEntity(info, basketItem);
        return toDto(basketItemRepository.save(basketItem));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "basket-items", key = "#id"),
            @CacheEvict(value = "basket-by-user", allEntries = true)
    })
    public void delete(int id) {
        BasketItem entity = basketItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Basket item not found"));
        basketItemRepository.delete(entity);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "basket-items", allEntries = true),
            @CacheEvict(value = "basket-by-user", key = "#userId")
    })
    public CheckoutResult checkout(int userId) {
        validateUserExists(userId);

        // Fetch user email for event
        String userEmail = getUserEmail(userId);

        List<BasketItem> items = basketItemRepository.findByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }

        List<OrderItemEvent> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (BasketItem item : items) {
            Map<String, Object> product = getProductInfo(item.getProductId());
            double price = ((Number) product.get("price")).doubleValue();
            String productName = (String) product.get("name");

            orderItems.add(new OrderItemEvent(
                    item.getProductId(),
                    productName,
                    item.getQuantity(),
                    price
            ));

            totalAmount += price * item.getQuantity();
        }

        String orderId = UUID.randomUUID().toString();

        // Serialize items to JSON
        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(orderItems);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize order items");
        }

        // Save order to database
        orderService.createOrder(orderId, userId, itemsJson, totalAmount);

        // Reduce stock for each item
        for (BasketItem item : items) {
            restTemplate.put(
                    STOCK_SERVICE_URL + "/products/" + item.getProductId() + "/reduce-stock",
                    Map.of("quantity", item.getQuantity())
            );
        }

        // Publish order created event
        eventPublisher.publishOrderCreated(
                new OrderCreatedEvent(orderId, userId, userEmail, orderItems, totalAmount)
        );

        // Clear basket
        basketItemRepository.deleteAll(items);

        CheckoutResult result = new CheckoutResult();
        result.setOrderId(orderId);
        result.setUserId(userId);
        result.setItems(items.stream().map(this::toDto).toList());
        result.setTotalAmount(totalAmount);
        return result;
    }

    private Map<String, Object> getProductInfo(int productId) {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                STOCK_SERVICE_URL + "/products/" + productId, Map.class);
        return response.getBody();
    }

    private String getUserEmail(int userId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    AUTH_SERVICE_URL + "/users/" + userId, Map.class);
            Map<String, Object> user = response.getBody();
            return user != null ? (String) user.get("email") : null;
        } catch (Exception e) {
            return null;
        }
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
