package com.dogu.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVOICE_EXCHANGE = "invoice.exchange";

    // Queues
    public static final String NOTIFICATION_USER_QUEUE = "notification.user.registered.queue";
    public static final String NOTIFICATION_ORDER_QUEUE = "notification.order.created.queue";
    public static final String NOTIFICATION_INVOICE_QUEUE = "notification.invoice.created.queue";

    // Routing Keys
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String INVOICE_CREATED_ROUTING_KEY = "invoice.created";

    // Exchanges
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange(INVOICE_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(NOTIFICATION_USER_QUEUE, true);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(NOTIFICATION_ORDER_QUEUE, true);
    }

    @Bean
    public Queue invoiceCreatedQueue() {
        return new Queue(NOTIFICATION_INVOICE_QUEUE, true);
    }

    // Bindings
    @Bean
    public Binding userBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(userExchange)
                .with(USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public Binding orderBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding invoiceBinding(Queue invoiceCreatedQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(invoiceCreatedQueue)
                .to(invoiceExchange)
                .with(INVOICE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
