package com.dogu.stock.product.web;

import com.dogu.stock.product.api.ProductDto;
import com.dogu.stock.product.api.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    ProductService productService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<ProductDto> allProducts = productService.getAll();
        List<ProductDto> lowStock = productService.getLowStock(5);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", allProducts.size());
        stats.put("lowStockCount", lowStock.size());
        return stats;
    }

    @GetMapping("/low-stock")
    public List<ProductResponse> getLowStock(@RequestParam(defaultValue = "5") int threshold) {
        return productService.getLowStock(threshold).stream().map(dto -> {
            ProductResponse response = new ProductResponse();
            response.id = dto.getId();
            response.name = dto.getName();
            response.description = dto.getDescription();
            response.price = dto.getPrice();
            response.quantity = dto.getQuantity();
            return response;
        }).toList();
    }
}
