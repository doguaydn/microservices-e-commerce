package com.dogu.invoice.invoice.api;

import java.util.List;

public interface InvoiceService {
    InvoiceDto createFromOrder(String orderId, int userId, String itemsJson, double totalAmount);
    InvoiceDto get(int id);
    InvoiceDto getByOrderId(String orderId);
    List<InvoiceDto> getByUserId(int userId);
    List<InvoiceDto> getAll();
}
