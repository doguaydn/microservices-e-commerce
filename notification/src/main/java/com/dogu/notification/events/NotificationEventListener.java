package com.dogu.notification.events;

import com.dogu.notification.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_USER_QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("========================================");
        logger.info("NOTIFICATION: New User Registered!");
        logger.info("User ID: {}", event.getUserId());
        logger.info("Email: {}", event.getEmail());
        logger.info("Name: {}", event.getName());
        logger.info("Timestamp: {}", event.getTimestamp());
        logger.info("========================================");

        simulateEmailSending("Welcome to our platform!", event.getEmail());
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_ORDER_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("========================================");
        logger.info("NOTIFICATION: New Order Created!");
        logger.info("Order ID: {}", event.getOrderId());
        logger.info("User ID: {}", event.getUserId());
        logger.info("Total Amount: {}", event.getTotalAmount());
        logger.info("Items Count: {}", event.getItems() != null ? event.getItems().size() : 0);
        logger.info("Timestamp: {}", event.getTimestamp());
        logger.info("========================================");

        simulateEmailSending("Your order has been received!", "user-" + event.getUserId() + "@example.com");
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_INVOICE_QUEUE)
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        logger.info("========================================");
        logger.info("NOTIFICATION: Invoice Created!");
        logger.info("Invoice ID: {}", event.getInvoiceId());
        logger.info("Order ID: {}", event.getOrderId());
        logger.info("User ID: {}", event.getUserId());
        logger.info("Total Amount: {}", event.getTotalAmount());
        logger.info("Timestamp: {}", event.getTimestamp());
        logger.info("========================================");

        simulateEmailSending("Your invoice is ready!", "user-" + event.getUserId() + "@example.com");
    }

    private void simulateEmailSending(String subject, String to) {
        logger.info("[EMAIL SIMULATION] Sending email...");
        logger.info("[EMAIL SIMULATION] To: {}", to);
        logger.info("[EMAIL SIMULATION] Subject: {}", subject);
        logger.info("[EMAIL SIMULATION] Email sent successfully (simulated)");
    }
}
