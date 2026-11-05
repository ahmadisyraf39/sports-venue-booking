package com.ahmadisyraf39.sportsbooking.booking_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BOOKING_CREATED_QUEUE = "booking.created.queue";
    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_CONFIRMED_ROUTING_KEY = "payment.confirmed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    public static final String BOOKING_PAYMENT_CONFIRMED_QUEUE = "booking.payment-confirmed.queue";
    public static final String BOOKING_PAYMENT_FAILED_QUEUE = "booking.payment-failed.queue";

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public Queue bookingCreatedQueue() {
        return new Queue(BOOKING_CREATED_QUEUE);
    }

    @Bean
    public Binding bookingCreatedBinding(Queue bookingCreatedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingCreatedQueue)
                .to(bookingExchange)
                .with(BOOKING_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

     @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue bookingPaymentConfirmedQueue() {
        return new Queue(BOOKING_PAYMENT_CONFIRMED_QUEUE);
    }

    @Bean
    public Binding bookingPaymentConfirmedBinding(Queue bookingPaymentConfirmedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(bookingPaymentConfirmedQueue)
                .to(paymentExchange)
                .with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Queue bookingPaymentFailedQueue() {
        return new Queue(BOOKING_PAYMENT_FAILED_QUEUE);
    }

    @Bean
    public Binding bookingPaymentFailedBinding(Queue bookingPaymentFailedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(bookingPaymentFailedQueue)
                .to(paymentExchange)
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }
}