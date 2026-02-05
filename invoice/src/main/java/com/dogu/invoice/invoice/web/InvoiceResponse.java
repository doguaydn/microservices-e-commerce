package com.dogu.invoice.invoice.web;

import java.time.LocalDateTime;

public class InvoiceResponse {
    public int id;
    public String orderId;
    public int userId;
    public String items;
    public double totalAmount;
    public String status;
    public LocalDateTime createdAt;
}
