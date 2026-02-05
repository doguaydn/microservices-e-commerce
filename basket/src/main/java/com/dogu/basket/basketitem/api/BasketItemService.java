package com.dogu.basket.basketitem.api;

import java.util.List;

public interface BasketItemService {
    public BasketItemDto save(BasketItemDto param);
    public BasketItemDto get(int id);
    public List<BasketItemDto> getAll();
    public List<BasketItemDto> getByUserId(int userId);
    public BasketItemDto update(BasketItemDto info);
    public void delete(int id);
}
