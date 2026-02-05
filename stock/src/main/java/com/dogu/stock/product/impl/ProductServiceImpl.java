package com.dogu.stock.product.impl;

import com.dogu.stock.product.api.ProductDto;
import com.dogu.stock.product.api.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public ProductDto save(ProductDto param) {
        Product product = toEntity(param, null);
        productRepository.save(product);
        return toDto(product);
    }

    @Override
    public ProductDto get(int id) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(entity);
    }

    @Override
    public List<ProductDto> getAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::toDto).toList();
    }

    @Override
    public ProductDto update(ProductDto info) {
        Product product = productRepository.findById(info.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product = toEntity(info, product);
        return toDto(productRepository.save(product));
    }

    @Override
    public void delete(int id) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(entity);
    }

    private Product toEntity(ProductDto request, Product entity) {
        if (entity == null) {
            entity = new Product();
        }
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setQuantity(request.getQuantity());
        return entity;
    }

    private ProductDto toDto(Product entity) {
        ProductDto dto = new ProductDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setQuantity(entity.getQuantity());
        return dto;
    }
}
