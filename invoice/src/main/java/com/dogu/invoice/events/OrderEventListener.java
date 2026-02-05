package com.dogu.invoice.events;

import com.dogu.invoice.config.RabbitMQConfig;
import com.dogu.invoice.invoice.api.InvoiceDto;
import com.dogu.invoice.invoice.api.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceEventPublisher eventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("Received order.created event: orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        try {
            String itemsJson = objectMapper.writeValueAsString(event.getItems());
            InvoiceDto invoice = invoiceService.createFromOrder(
                    event.getOrderId(),
                    event.getUserId(),
                    itemsJson,
                    event.getTotalAmount()
            );

            // Publish invoice created event
            eventPublisher.publishInvoiceCreated(
                    new InvoiceCreatedEvent(
                            invoice.getId(),
                            invoice.getOrderId(),
                            invoice.getUserId(),
                            invoice.getTotalAmount()
                    )
            );

            logger.info("Invoice created: id={}", invoice.getId());
        } catch (Exception e) {
            logger.error("Error creating invoice: {}", e.getMessage());
        }
    }
}
