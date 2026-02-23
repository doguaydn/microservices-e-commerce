package com.dogu.stock.product.impl;

import com.dogu.stock.product.api.ProductDto;
import com.dogu.stock.product.api.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products-all", allEntries = true)
    })
    public ProductDto save(ProductDto param) {
        Product product = toEntity(param, null);
        productRepository.save(product);
        return toDto(product);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductDto get(int id) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(entity);
    }

    @Override
    @Cacheable(value = "products-all")
    public List<ProductDto> getAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::toDto).toList();
    }

    @Override
    @Caching(
            put = {@CachePut(value = "products", key = "#info.id")},
            evict = {@CacheEvict(value = "products-all", allEntries = true)}
    )
    public ProductDto update(ProductDto info) {
        Product product = productRepository.findById(info.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product = toEntity(info, product);
        return toDto(productRepository.save(product));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products-all", allEntries = true)
    })
    public void delete(int id) {
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(entity);
    }

    @Override
    @Caching(
            put = {@CachePut(value = "products", key = "#id")},
            evict = {@CacheEvict(value = "products-all", allEntries = true)}
    )
    public ProductDto reduceStock(int id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getQuantity());
        }
        product.setQuantity(product.getQuantity() - quantity);
        return toDto(productRepository.save(product));
    }

    @Override
    public List<ProductDto> getLowStock(int threshold) {
        return productRepository.findByQuantityLessThanEqual(threshold)
                .stream().map(this::toDto).toList();
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
