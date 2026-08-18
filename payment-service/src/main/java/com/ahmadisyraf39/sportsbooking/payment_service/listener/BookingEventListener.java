package com.ahmadisyraf39.sportsbooking.payment_service.listener;

import com.ahmadisyraf39.sportsbooking.payment_service.config.RabbitMQConfig;
import com.ahmadisyraf39.sportsbooking.payment_service.event.BookingCreatedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_CREATED_QUEUE)
    public void handleBookingCreated(BookingCreatedEvent event) {
        paymentService.processBookingCreated(event);
    }
}
