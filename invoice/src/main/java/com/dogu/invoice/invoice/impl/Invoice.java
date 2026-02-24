package com.dogu.invoice.invoice.impl;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String orderId;
    private int userId;

    @Column(columnDefinition = "TEXT")
    private String items;

    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String invoiceSlug;

    public Invoice() {
        this.createdAt = LocalDateTime.now();
        this.status = "CREATED";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getInvoiceSlug() {
        return invoiceSlug;
    }

    public void setInvoiceSlug(String invoiceSlug) {
        this.invoiceSlug = invoiceSlug;
    }
}
