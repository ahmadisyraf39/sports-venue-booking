package com.ahmadisyraf39.sportsbooking.notification_service.listener;

import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Test
    void shouldDelegateToNotificationService_WhenPaymentConfirmedEventReceived() {
        PaymentConfirmedEvent event = new PaymentConfirmedEvent(1L, "booking-1", 10L, new BigDecimal("25.00"));

        paymentEventListener.handlePaymentConfirmed(event);

        verify(notificationService).processPaymentConfirmed(event);
    }

    @Test
    void shouldDelegateToNotificationService_WhenPaymentFailedEventReceived() {
        PaymentFailedEvent event = new PaymentFailedEvent(1L, "booking-1", 10L, new BigDecimal("25.00"));

        paymentEventListener.handlePaymentFailed(event);

        verify(notificationService).processPaymentFailed(event);
    }
}
