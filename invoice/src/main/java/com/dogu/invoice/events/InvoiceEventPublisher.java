package com.dogu.invoice.events;

import com.dogu.invoice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishInvoiceCreated(InvoiceCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVOICE_EXCHANGE,
                RabbitMQConfig.INVOICE_CREATED_ROUTING_KEY,
                event
        );
    }
}
