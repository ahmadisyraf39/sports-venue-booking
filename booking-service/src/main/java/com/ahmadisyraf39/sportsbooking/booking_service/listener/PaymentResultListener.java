package com.ahmadisyraf39.sportsbooking.booking_service.listener;

import com.ahmadisyraf39.sportsbooking.booking_service.config.RabbitMQConfig;
import com.ahmadisyraf39.sportsbooking.booking_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.booking_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultListener {

    private final BookingService bookingService;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_PAYMENT_CONFIRMED_QUEUE)
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        bookingService.confirmBooking(event.getBookingId());
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        bookingService.cancelBooking(event.getBookingId());
    }
}