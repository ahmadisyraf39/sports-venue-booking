package com.ahmadisyraf39.sportsbooking.booking_service.service;

import com.ahmadisyraf39.sportsbooking.booking_service.dto.external.CourtDetails;
import com.ahmadisyraf39.sportsbooking.booking_service.dto.request.CreateBookingRequest;
import com.ahmadisyraf39.sportsbooking.booking_service.dto.response.BookingResponse;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.Booking;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.BookingStatus;
import com.ahmadisyraf39.sportsbooking.booking_service.exception.BookingNotFoundException;
import com.ahmadisyraf39.sportsbooking.booking_service.exception.SlotUnavailableException;
import com.ahmadisyraf39.sportsbooking.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestClient venueServiceRestClient;
    private final BookingLockService bookingLockService;

    public BookingResponse createBooking(CreateBookingRequest request) {
        boolean lockAcquired = bookingLockService.acquireLock(
                request.getCourtId(), request.getBookingDate(), request.getStartTime());

        if (!lockAcquired) {
            throw new SlotUnavailableException("This slot is currently being booked by someone else, please try again");
        }

        try {
            CourtDetails court = fetchCourtDetails(request.getCourtId());

            LocalTime endTime = request.getStartTime().plusHours(request.getDurationHours());
            BigDecimal totalPrice = court.getHourlyPrice().multiply(BigDecimal.valueOf(request.getDurationHours()));

            checkSlotAvailable(request.getCourtId(), request.getBookingDate(), request.getStartTime(), endTime);

            Booking booking = new Booking();
            booking.setCourtId(request.getCourtId());
            booking.setVenueId(request.getVenueId());
            booking.setUserId(request.getUserId());
            booking.setBookingDate(request.getBookingDate());
            booking.setStartTime(request.getStartTime());
            booking.setEndTime(endTime);
            booking.setStatus(BookingStatus.PENDING);
            booking.setTotalPrice(totalPrice);

            return toBookingResponse(bookingRepository.save(booking));
        } finally {
            bookingLockService.releaseLock(request.getCourtId(), request.getBookingDate(), request.getStartTime());
        }
    }

    public BookingResponse getBookingById(String id) {
        return toBookingResponse(findBookingById(id));
    }

    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toBookingResponse)
                .toList();
    }

    private CourtDetails fetchCourtDetails(Long courtId) {
        try {
            return venueServiceRestClient.get()
                    .uri("/api/courts/{id}", courtId)
                    .retrieve()
                    .body(CourtDetails.class);
        } catch (Exception e) {
            throw new BookingNotFoundException("Court not found with id: " + courtId);
        }
    }

    private void checkSlotAvailable(Long courtId, LocalDate bookingDate, LocalTime newStart, LocalTime newEnd) {
        List<Booking> existingBookings = bookingRepository
                .findByCourtIdAndBookingDateAndStatusNot(courtId, bookingDate, BookingStatus.CANCELLED);

        boolean hasConflict = existingBookings.stream()
                .anyMatch(b -> b.getStartTime().isBefore(newEnd) && b.getEndTime().isAfter(newStart));

        if (hasConflict) {
            throw new SlotUnavailableException("This court is already booked for the requested time slot");
        }
    }

    Booking findBookingById(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .venueId(booking.getVenueId())
                .courtId(booking.getCourtId())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}