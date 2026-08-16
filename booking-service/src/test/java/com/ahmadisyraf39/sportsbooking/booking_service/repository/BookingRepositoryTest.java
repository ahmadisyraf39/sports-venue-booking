package com.ahmadisyraf39.sportsbooking.booking_service.repository;

import com.ahmadisyraf39.sportsbooking.booking_service.entity.Booking;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
public class BookingRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void cleanUp() {
        bookingRepository.deleteAll();
    }

    private Booking newBooking(Long courtId, LocalDate date, BookingStatus status) {
        Booking booking = new Booking();
        booking.setCourtId(courtId);
        booking.setVenueId(1L);
        booking.setUserId(10L);
        booking.setBookingDate(date);
        booking.setStartTime(LocalTime.of(14, 0));
        booking.setEndTime(LocalTime.of(15, 0));
        booking.setStatus(status);
        booking.setTotalPrice(new BigDecimal("25.00"));
        return booking;
    }

    @Test
    void shouldSaveAndRetrieveBooking() {
        Booking booking = newBooking(5L, LocalDate.of(2026, 8, 20), BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        assertThat(saved.getId()).isNotNull();
        assertThat(bookingRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindBookingsByCourtIdAndDate() {
        bookingRepository.save(newBooking(5L, LocalDate.of(2026, 8, 20), BookingStatus.PENDING));

        List<Booking> found = bookingRepository.findByCourtIdAndBookingDate(5L, LocalDate.of(2026, 8, 20));

        assertThat(found).hasSize(1);
    }

    @Test
    void shouldExcludeCancelledBookingsWhenCheckingConflicts() {
        bookingRepository.save(newBooking(5L, LocalDate.of(2026, 8, 20), BookingStatus.CANCELLED));

        List<Booking> activeBookings = bookingRepository.findByCourtIdAndBookingDateAndStatusNot(
                5L, LocalDate.of(2026, 8, 20), BookingStatus.CANCELLED);

        assertThat(activeBookings).isEmpty();
    }

}
