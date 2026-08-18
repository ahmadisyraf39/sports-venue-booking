package com.ahmadisyraf39.sportsbooking.payment_service.repository;

import com.ahmadisyraf39.sportsbooking.payment_service.entity.Payment;
import com.ahmadisyraf39.sportsbooking.payment_service.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class PaymentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment newPayment(String bookingId, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(1L);
        payment.setPaymentTotal(new BigDecimal("25.00"));
        payment.setStatus(status);
        return payment;
    }

    @Test
    void shouldSaveAndRetrievePaymentById() {
        Payment payment = newPayment("booking-1", PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(paymentRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindPaymentsByBookingId() {
        paymentRepository.save(newPayment("booking-2", PaymentStatus.CONFIRMED));

        List<Payment> found = paymentRepository.findByBookingId("booking-2");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @Test
    void shouldReturnEmptyWhenBookingIdNotFound() {
        List<Payment> found = paymentRepository.findByBookingId("does-not-exist");

        assertThat(found).isEmpty();
    }
}
