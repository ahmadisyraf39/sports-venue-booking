package com.ahmadisyraf39.sportsbooking.payment_service.service;

import com.ahmadisyraf39.sportsbooking.payment_service.dto.response.PaymentResponse;
import com.ahmadisyraf39.sportsbooking.payment_service.entity.Payment;
import com.ahmadisyraf39.sportsbooking.payment_service.entity.PaymentStatus;
import com.ahmadisyraf39.sportsbooking.payment_service.event.BookingCreatedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.exception.PaymentNotFoundException;
import com.ahmadisyraf39.sportsbooking.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Payment existingPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBookingId("booking-1");
        payment.setUserId(10L);
        payment.setVenueId(2L);
        payment.setPaymentTotal(new BigDecimal("25.00"));
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    private BookingCreatedEvent sampleEvent() {
        return new BookingCreatedEvent(
                "booking-1",
                10L,
                2L,
                3L,
                LocalDate.of(2026, 8, 20),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                new BigDecimal("25.00")
        );
    }

    @Nested
    class ProcessBookingCreated {

        @Test
        void shouldCreatePaymentAsConfirmed_AndPublishPaymentConfirmedEvent() {
            when(paymentRepository.findByBookingId("booking-1")).thenReturn(List.of());
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment p = invocation.getArgument(0);
                if (p.getId() == null) {
                    p.setId(1L);
                }
                return p;
            });

            paymentService.processBookingCreated(sampleEvent());

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository, times(2)).save(paymentCaptor.capture());
            assertThat(paymentCaptor.getValue().getVenueId()).isEqualTo(2L);
            verify(rabbitTemplate).convertAndSend(
                    eq("payment.exchange"), eq("payment.confirmed"), any(PaymentConfirmedEvent.class));
        }

        @Test
        void shouldSkip_WhenPaymentAlreadyExistsForBookingId() {
            when(paymentRepository.findByBookingId("booking-1")).thenReturn(List.of(existingPayment()));

            paymentService.processBookingCreated(sampleEvent());

            verify(paymentRepository, never()).save(any(Payment.class));
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }

    @Nested
    class GetPaymentById {

        @Test
        void shouldReturnPayment_WhenExists() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(existingPayment()));

            PaymentResponse response = paymentService.getPaymentById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getBookingId()).isEqualTo("booking-1");
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    @Nested
    class GetPaymentsByBookingId {

        @Test
        void shouldReturnPayments_ForBookingId() {
            when(paymentRepository.findByBookingId("booking-1")).thenReturn(List.of(existingPayment()));

            List<PaymentResponse> responses = paymentService.getPaymentsByBookingId("booking-1");

            assertThat(responses).hasSize(1);
        }
    }
}
