package com.ahmadisyraf39.sportsbooking.payment_service.repository;

import com.ahmadisyraf39.sportsbooking.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(String bookingId);

}
