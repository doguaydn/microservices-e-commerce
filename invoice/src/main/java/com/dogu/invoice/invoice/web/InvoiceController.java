package com.dogu.invoice.invoice.web;

import com.dogu.invoice.invoice.api.InvoiceDto;
import com.dogu.invoice.invoice.api.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService service;

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable int id) {
        return toResponse(service.get(id));
    }

    @GetMapping("/order/{orderId}")
    public InvoiceResponse getByOrderId(@PathVariable String orderId) {
        return toResponse(service.getByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    public List<InvoiceResponse> getByUserId(@PathVariable int userId) {
        return service.getByUserId(userId).stream()
                .map(this::toResponse).toList();
    }

    @GetMapping
    public List<InvoiceResponse> getAll() {
        return service.getAll().stream()
                .map(this::toResponse).toList();
    }

    private InvoiceResponse toResponse(InvoiceDto dto) {
        InvoiceResponse response = new InvoiceResponse();
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
