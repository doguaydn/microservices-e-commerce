package com.dogu.invoice.events;

import java.time.LocalDateTime;

public class InvoiceCreatedEvent {
    private int invoiceId;
    private String orderId;
    private int userId;
    private String email;
    private double totalAmount;
    private String items;
    private String invoiceSlug;
    private LocalDateTime timestamp;

    public InvoiceCreatedEvent() {
    }

    public InvoiceCreatedEvent(int invoiceId, String orderId, int userId, String email, double totalAmount, String items, String invoiceSlug) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.userId = userId;
        this.email = email;
        this.totalAmount = totalAmount;
        this.items = items;
        this.invoiceSlug = invoiceSlug;
        this.timestamp = LocalDateTime.now();
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getInvoiceSlug() {
        return invoiceSlug;
    }

    public void setInvoiceSlug(String invoiceSlug) {
        this.invoiceSlug = invoiceSlug;
    }
}
