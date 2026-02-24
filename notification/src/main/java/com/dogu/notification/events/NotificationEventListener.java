package com.dogu.notification.events;

import com.dogu.notification.config.RabbitMQConfig;
import com.dogu.notification.service.EmailService;
import com.dogu.notification.service.EmailTemplateService;
import com.dogu.notification.service.EmailTemplateService.OrderItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_USER_QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("NOTIFICATION: New User Registered! userId={}, email={}", event.getUserId(), event.getEmail());

        String html = templateService.buildWelcomeEmail(event.getName());
        emailService.sendHtmlEmail(event.getEmail(), "Welcome to NovaMart!", html);
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_ORDER_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("NOTIFICATION: New Order Created! orderId={}, userId={}, email={}", event.getOrderId(), event.getUserId(), event.getEmail());

        if (event.getEmail() == null) {
            logger.warn("No email found for userId={}, skipping email", event.getUserId());
            return;
        }

        List<OrderItem> items = new ArrayList<>();
        if (event.getItems() != null) {
            for (OrderItemEvent item : event.getItems()) {
                items.add(new OrderItem(item.getProductName(), item.getQuantity(), item.getPrice()));
            }
        }

        String html = templateService.buildOrderEmail(event.getOrderId(), items, event.getTotalAmount());
        emailService.sendHtmlEmail(event.getEmail(), "Order Confirmed - NovaMart", html);
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_INVOICE_QUEUE)
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        logger.info("NOTIFICATION: Invoice Created! invoiceSlug={}, email={}", event.getInvoiceSlug(), event.getEmail());

        if (event.getEmail() == null) {
            logger.warn("No email found for userId={}, skipping email", event.getUserId());
            return;
        }

        List<OrderItem> items = new ArrayList<>();
        if (event.getItems() != null) {
            try {
                JsonNode itemsNode = objectMapper.readTree(event.getItems());
                if (itemsNode.isArray()) {
                    for (JsonNode item : itemsNode) {
                        String productName = item.has("productName") ? item.get("productName").asText() : "Unknown";
                        int quantity = item.has("quantity") ? item.get("quantity").asInt() : 1;
                        double price = item.has("price") ? item.get("price").asDouble() : 0.0;
                        items.add(new OrderItem(productName, quantity, price));
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not parse items JSON: {}", e.getMessage());
            }
        }

        String html = templateService.buildInvoiceEmail(event.getInvoiceSlug(), items, event.getTotalAmount());
        emailService.sendHtmlEmail(event.getEmail(), "Invoice " + event.getInvoiceSlug() + " - NovaMart", html);
    }
}
