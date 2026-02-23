package com.dogu.notification.events;

import com.dogu.notification.config.RabbitMQConfig;
import com.dogu.notification.service.EmailService;
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
        logger.info("NOTIFICATION: Invoice Created! invoiceId={}, orderId={}, email={}", event.getInvoiceId(), event.getOrderId(), event.getEmail());

        if (event.getEmail() == null) {
            logger.warn("No email found for userId={}, skipping email", event.getUserId());
            return;
        }

        String body = String.format(
                "Your invoice has been generated.\n\nInvoice ID: %d\nOrder ID: %s\nTotal Amount: $%.2f\n\nThank you for your purchase!",
                event.getInvoiceId(),
                event.getOrderId(),
                event.getTotalAmount()
        );
        emailService.sendEmail(event.getEmail(), "Invoice #" + event.getInvoiceId() + " - E-Commerce", body);
    }
}
