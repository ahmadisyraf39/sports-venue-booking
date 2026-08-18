package com.ahmadisyraf39.sportsbooking.payment_service.listener;

import com.ahmadisyraf39.sportsbooking.payment_service.event.BookingCreatedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingEventListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingEventListener bookingEventListener;

    @Test
    void shouldDelegateToPaymentService_WhenBookingCreatedEventReceived() {
        BookingCreatedEvent event = new BookingCreatedEvent(
                "booking-1",
                10L,
                2L,
                3L,
                LocalDate.of(2026, 8, 20),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                new BigDecimal("25.00")
        );

        bookingEventListener.handleBookingCreated(event);

        verify(paymentService).processBookingCreated(event);
    }
}
