package com.dogu.invoice.invoice.impl;

import com.dogu.invoice.invoice.api.InvoiceDto;
import com.dogu.invoice.invoice.api.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "invoices-by-user", key = "#userId"),
            @CacheEvict(value = "invoices-all", allEntries = true)
    })
    public InvoiceDto createFromOrder(String orderId, int userId, String itemsJson, double totalAmount) {
        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setUserId(userId);
        invoice.setItems(itemsJson);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("CREATED");
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setInvoiceSlug("INV-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));

        invoiceRepository.save(invoice);
        return toDto(invoice);
    }

    @Override
    @Cacheable(value = "invoices", key = "#id")
    public InvoiceDto get(int id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return toDto(invoice);
    }

    @Override
    @Cacheable(value = "invoices-by-order", key = "#orderId")
    public InvoiceDto getByOrderId(String orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return toDto(invoice);
    }

    @Override
    @Cacheable(value = "invoices-by-user", key = "#userId")
    public List<InvoiceDto> getByUserId(int userId) {
        return invoiceRepository.findByUserId(userId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Cacheable(value = "invoices-all")
    public List<InvoiceDto> getAll() {
        return invoiceRepository.findAll()
                .stream().map(this::toDto).toList();
    }

    private InvoiceDto toDto(Invoice entity) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setUserId(entity.getUserId());
        dto.setItems(entity.getItems());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setInvoiceSlug(entity.getInvoiceSlug());
        return dto;
    }
}
