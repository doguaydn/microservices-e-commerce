package com.dogu.notification.events;

import com.dogu.notification.config.RabbitMQConfig;
import com.dogu.notification.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_USER_QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("NOTIFICATION: New User Registered! userId={}, email={}", event.getUserId(), event.getEmail());

        String body = String.format(
                "Welcome %s!\n\nYour account has been created successfully.\n\nThank you for joining us!",
                event.getName()
        );
        emailService.sendEmail(event.getEmail(), "Welcome to E-Commerce!", body);
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_ORDER_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("NOTIFICATION: New Order Created! orderId={}, userId={}, email={}", event.getOrderId(), event.getUserId(), event.getEmail());

        if (event.getEmail() == null) {
            logger.warn("No email found for userId={}, skipping email", event.getUserId());
            return;
        }

        StringBuilder body = new StringBuilder();
        body.append("Your order has been placed successfully!\n\n");
        body.append("Order ID: ").append(event.getOrderId()).append("\n");
        body.append("Total Amount: $").append(String.format("%.2f", event.getTotalAmount())).append("\n\n");

        if (event.getItems() != null) {
            body.append("Items:\n");
            for (OrderItemEvent item : event.getItems()) {
                body.append("- ").append(item.getProductName())
                        .append(" x").append(item.getQuantity())
                        .append(" ($").append(String.format("%.2f", item.getPrice())).append(")\n");
            }
        }

        body.append("\nThank you for your purchase!");
        emailService.sendEmail(event.getEmail(), "Order Confirmation - " + event.getOrderId(), body.toString());
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_INVOICE_QUEUE)
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        logger.info("NOTIFICATION: Invoice Created! invoiceSlug={}, email={}", event.getInvoiceSlug(), event.getEmail());

        if (event.getEmail() == null) {
            logger.warn("No email found for userId={}, skipping email", event.getUserId());
            return;
        }

        StringBuilder body = new StringBuilder();
        body.append("Your invoice has been generated.\n\n");
        body.append("Invoice: ").append(event.getInvoiceSlug()).append("\n");
        body.append("Total Amount: $").append(String.format("%.2f", event.getTotalAmount())).append("\n\n");

        // Parse items JSON and list products
        if (event.getItems() != null) {
            try {
                JsonNode items = objectMapper.readTree(event.getItems());
                if (items.isArray()) {
                    body.append("Items:\n");
                    for (JsonNode item : items) {
                        String productName = item.has("productName") ? item.get("productName").asText() : "Unknown";
                        int quantity = item.has("quantity") ? item.get("quantity").asInt() : 1;
                        double price = item.has("price") ? item.get("price").asDouble() : 0.0;
                        body.append("- ").append(productName)
                                .append(" x").append(quantity)
                                .append(" ($").append(String.format("%.2f", price)).append(")\n");
                    }
                    body.append("\n");
                }
            } catch (Exception e) {
                logger.warn("Could not parse items JSON: {}", e.getMessage());
            }
        }

        body.append("Thank you for your purchase!");
        emailService.sendEmail(event.getEmail(), "Invoice " + event.getInvoiceSlug() + " - NovaMart", body.toString());
    }
}
