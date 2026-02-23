package com.dogu.stock.product.web;

import com.dogu.stock.product.api.ProductDto;
import com.dogu.stock.product.api.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    ProductService service;

    @PostMapping
    public ProductResponse save(@RequestBody ProductRequest info) {
        ProductDto productDto = service.save(toDto(info));
        return toResponse(productDto);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable int id) {
        ProductDto productDto = service.get(id);
        return toResponse(productDto);
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        List<ProductDto> products = service.getAll();
        return products.stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable int id, @RequestBody ProductRequest info) {
        ProductDto dto = toDto(info);
        dto.setId(id);
        ProductDto productDto = service.update(dto);
        return toResponse(productDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }

    @PutMapping("/{id}/reduce-stock")
    public ProductResponse reduceStock(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        int quantity = body.get("quantity");
        ProductDto dto = service.reduceStock(id, quantity);
        return toResponse(dto);
    }

    public ProductResponse toResponse(ProductDto dto) {
        ProductResponse response = new ProductResponse();
        response.id = dto.getId();
        response.name = dto.getName();
        response.description = dto.getDescription();
        response.price = dto.getPrice();
        response.quantity = dto.getQuantity();
        return response;
    }

    public ProductDto toDto(ProductRequest request) {
        ProductDto dto = new ProductDto();
        dto.setName(request.name);
        dto.setDescription(request.description);
        dto.setPrice(request.price);
        dto.setQuantity(request.quantity);
        return dto;
    }
}
