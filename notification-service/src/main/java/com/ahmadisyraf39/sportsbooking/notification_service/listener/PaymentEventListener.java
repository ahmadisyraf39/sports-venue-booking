package com.ahmadisyraf39.sportsbooking.notification_service.listener;

import com.ahmadisyraf39.sportsbooking.notification_service.config.RabbitMQConfig;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_CONFIRMED_QUEUE)
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        notificationService.processPaymentConfirmed(event);
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        notificationService.processPaymentFailed(event);
    }
}
