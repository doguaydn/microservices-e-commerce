package com.dogu.basket.order.web;

import com.dogu.basket.order.api.OrderDto;
import com.dogu.basket.order.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        return toResponse(orderService.getByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getByUserId(@PathVariable int userId) {
        return orderService.getByUserId(userId).stream().map(this::toResponse).toList();
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.getAll().stream().map(this::toResponse).toList();
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return toResponse(orderService.updateStatus(orderId, status));
    }

    @DeleteMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable String orderId) {
        return toResponse(orderService.cancelOrder(orderId));
    }

    private OrderResponse toResponse(OrderDto dto) {
        OrderResponse response = new OrderResponse();
        response.id = dto.getId();
        response.orderId = dto.getOrderId();
        response.userId = dto.getUserId();
        response.items = dto.getItems();
        response.totalAmount = dto.getTotalAmount();
        response.status = dto.getStatus();
        response.createdAt = dto.getCreatedAt();
        return response;
    }
}
