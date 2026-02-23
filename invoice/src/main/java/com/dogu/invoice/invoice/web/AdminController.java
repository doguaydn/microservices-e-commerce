package com.dogu.invoice.invoice.web;

import com.dogu.invoice.invoice.api.InvoiceDto;
import com.dogu.invoice.invoice.api.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    InvoiceService invoiceService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<InvoiceDto> allInvoices = invoiceService.getAll();
        double totalAmount = allInvoices.stream().mapToDouble(InvoiceDto::getTotalAmount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInvoices", allInvoices.size());
        stats.put("totalAmount", totalAmount);
        return stats;
    }
}
