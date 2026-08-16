package com.ahmadisyraf39.sportsbooking.booking_service.repository;

import com.ahmadisyraf39.sportsbooking.booking_service.entity.Booking;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByCourtIdAndBookingDate(Long courtId, LocalDate bookingDate);

    List<Booking> findByCourtIdAndBookingDateAndStatusNot(Long courtId, LocalDate bookingDate, BookingStatus status);

}
