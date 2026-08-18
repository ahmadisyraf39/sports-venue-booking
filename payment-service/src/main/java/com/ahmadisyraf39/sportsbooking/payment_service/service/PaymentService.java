package com.ahmadisyraf39.sportsbooking.payment_service.service;

import com.ahmadisyraf39.sportsbooking.payment_service.config.RabbitMQConfig;
import com.ahmadisyraf39.sportsbooking.payment_service.dto.response.PaymentResponse;
import com.ahmadisyraf39.sportsbooking.payment_service.entity.Payment;
import com.ahmadisyraf39.sportsbooking.payment_service.entity.PaymentStatus;
import com.ahmadisyraf39.sportsbooking.payment_service.event.BookingCreatedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.payment_service.exception.PaymentNotFoundException;
import com.ahmadisyraf39.sportsbooking.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void processBookingCreated(BookingCreatedEvent event) {
        if (!paymentRepository.findByBookingId(event.getBookingId()).isEmpty()) {
            // Duplicate delivery of the same BookingCreated message (e.g. after a
            // consumer restart before the ack was sent) - skip to stay idempotent.
            return;
        }

        Payment payment = new Payment();
        payment.setBookingId(event.getBookingId());
        payment.setUserId(event.getUserId());
        payment.setVenueId(event.getVenueId());
        payment.setPaymentTotal(event.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        boolean succeeded = simulatePaymentProcessing();
        savedPayment.setStatus(succeeded ? PaymentStatus.CONFIRMED : PaymentStatus.FAILED);
        paymentRepository.save(savedPayment);

        publishPaymentResultEvent(savedPayment);
    }

    private boolean simulatePaymentProcessing() {
        // Mock payment gateway: no real charge attempt, always succeeds.
        return true;
    }

    private void publishPaymentResultEvent(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            PaymentConfirmedEvent event = new PaymentConfirmedEvent(
                    payment.getId(),
                    payment.getBookingId(),
                    payment.getUserId(),
                    payment.getPaymentTotal()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_CONFIRMED_ROUTING_KEY,
                    event
            );
        } else {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    payment.getId(),
                    payment.getBookingId(),
                    payment.getUserId(),
                    payment.getPaymentTotal()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY,
                    event
            );
        }
    }

    public PaymentResponse getPaymentById(Long id) {
        return toPaymentResponse(findPaymentById(id));
    }

    public List<PaymentResponse> getPaymentsByBookingId(String bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .venueId(payment.getVenueId())
                .userId(payment.getUserId())
                .paymentTotal(payment.getPaymentTotal())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
