package com.dogu.basket.order.web;

import com.dogu.basket.order.api.OrderDto;
import com.dogu.basket.order.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    OrderService orderService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<OrderDto> allOrders = orderService.getAll();
        double totalRevenue = allOrders.stream().mapToDouble(OrderDto::getTotalAmount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", allOrders.size());
        stats.put("pendingOrders", orderService.countByStatus("PENDING"));
        stats.put("confirmedOrders", orderService.countByStatus("CONFIRMED"));
        stats.put("deliveredOrders", orderService.countByStatus("DELIVERED"));
        stats.put("cancelledOrders", orderService.countByStatus("CANCELLED"));
        stats.put("totalRevenue", totalRevenue);
        return stats;
    }

    @GetMapping("/orders")
    public List<OrderResponse> getByStatus(@RequestParam(required = false) String status) {
        List<OrderDto> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getByStatus(status);
        } else {
            orders = orderService.getAll();
        }
        return orders.stream().map(dto -> {
            OrderResponse response = new OrderResponse();
            response.id = dto.getId();
            response.orderId = dto.getOrderId();
            response.userId = dto.getUserId();
            response.items = dto.getItems();
            response.totalAmount = dto.getTotalAmount();
            response.status = dto.getStatus();
            response.createdAt = dto.getCreatedAt();
            return response;
        }).toList();
    }
}
