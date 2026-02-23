package com.dogu.stock.product.api;

import java.util.List;

public interface ProductService {
    public ProductDto save(ProductDto param);
    public ProductDto get(int id);
    public List<ProductDto> getAll();
    public ProductDto update(ProductDto info);
    public void delete(int id);
    public ProductDto reduceStock(int id, int quantity);
    public List<ProductDto> getLowStock(int threshold);
}
