package com.ahmadisyraf39.sportsbooking.booking_service.service;

import com.ahmadisyraf39.sportsbooking.booking_service.dto.external.CourtDetails;
import com.ahmadisyraf39.sportsbooking.booking_service.dto.request.CreateBookingRequest;
import com.ahmadisyraf39.sportsbooking.booking_service.dto.response.BookingResponse;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.Booking;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.BookingStatus;
import com.ahmadisyraf39.sportsbooking.booking_service.event.BookingCreatedEvent;
import com.ahmadisyraf39.sportsbooking.booking_service.exception.BookingNotFoundException;
import com.ahmadisyraf39.sportsbooking.booking_service.exception.SlotUnavailableException;
import com.ahmadisyraf39.sportsbooking.booking_service.repository.BookingRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestClient venueServiceRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private BookingLockService bookingLockService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingService bookingService;

    private Booking existingBooking() {
        Booking booking = new Booking();
        booking.setId("booking-1");
        booking.setUserId(1L);
        booking.setVenueId(2L);
        booking.setCourtId(3L);
        booking.setBookingDate(LocalDate.of(2026, 8, 20));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(11, 0));
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(new BigDecimal("25.00"));
        return booking;
    }

    private CreateBookingRequest sampleRequest() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCourtId(3L);
        request.setVenueId(2L);
        request.setUserId(1L);
        request.setBookingDate(LocalDate.of(2026, 8, 20));
        request.setStartTime(LocalTime.of(14, 0));
        request.setDurationHours(1);
        return request;
    }

    private void mockVenueServiceCall(CourtDetails courtDetails) {
        when(venueServiceRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CourtDetails.class)).thenReturn(courtDetails);
    }

    @Nested
    class CreateBooking {

        @Test
        void shouldCreateBooking_WhenLockAcquiredAndSlotIsFree() {
            when(bookingLockService.acquireLock(3L, LocalDate.of(2026, 8, 20), LocalTime.of(14, 0)))
                    .thenReturn(true);

            CourtDetails court = new CourtDetails(3L, 2L, "Court A", "BADMINTON", new BigDecimal("25.00"));
            mockVenueServiceCall(court);

            when(bookingRepository.findByCourtIdAndBookingDateAndStatusNot(
                    eq(3L), eq(LocalDate.of(2026, 8, 20)), eq(BookingStatus.CANCELLED)))
                    .thenReturn(List.of());

            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
                Booking b = invocation.getArgument(0);
                b.setId("new-booking-id");
                return b;
            });

            BookingResponse response = bookingService.createBooking(sampleRequest());

            assertThat(response.getId()).isEqualTo("new-booking-id");
            verify(bookingLockService).releaseLock(3L, LocalDate.of(2026, 8, 20), LocalTime.of(14, 0));
            verify(rabbitTemplate).convertAndSend(
                    eq("booking.exchange"), eq("booking.created"), any(BookingCreatedEvent.class));
        }

        @Test
        void shouldThrowSlotUnavailable_WhenLockNotAcquired() {
            when(bookingLockService.acquireLock(3L, LocalDate.of(2026, 8, 20), LocalTime.of(14, 0)))
                    .thenReturn(false);

            assertThatThrownBy(() -> bookingService.createBooking(sampleRequest()))
                    .isInstanceOf(SlotUnavailableException.class);

            verify(bookingRepository, never()).save(any(Booking.class));
            verify(bookingLockService, never()).releaseLock(any(), any(), any());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        @Test
        void shouldReleaseLock_WhenConflictDetectedAfterLockAcquired() {
            when(bookingLockService.acquireLock(3L, LocalDate.of(2026, 8, 20), LocalTime.of(14, 0)))
                    .thenReturn(true);

            CourtDetails court = new CourtDetails(3L, 2L, "Court A", "BADMINTON", new BigDecimal("25.00"));
            mockVenueServiceCall(court);

            Booking conflicting = existingBooking();
            conflicting.setStartTime(LocalTime.of(14, 0));
            conflicting.setEndTime(LocalTime.of(15, 0));

            when(bookingRepository.findByCourtIdAndBookingDateAndStatusNot(
                    eq(3L), eq(LocalDate.of(2026, 8, 20)), eq(BookingStatus.CANCELLED)))
                    .thenReturn(List.of(conflicting));

            assertThatThrownBy(() -> bookingService.createBooking(sampleRequest()))
                    .isInstanceOf(SlotUnavailableException.class);

            verify(bookingRepository, never()).save(any(Booking.class));
            verify(bookingLockService).releaseLock(3L, LocalDate.of(2026, 8, 20), LocalTime.of(14, 0));
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }

    @Nested
    class GetBookingById {
        @Test
        void shouldReturnBooking_WhenExists() {
            when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(existingBooking()));
            BookingResponse response = bookingService.getBookingById("booking-1");
            assertThat(response.getId()).isEqualTo("booking-1");
        }

        @Test
        void shouldThrowException_WhenNotFound() {
            when(bookingRepository.findById("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bookingService.getBookingById("missing"))
                    .isInstanceOf(BookingNotFoundException.class);
        }
    }

    @Nested
    class GetBookingsByUserId {
        @Test
        void shouldReturnBookings_ForUser() {
            when(bookingRepository.findByUserId(1L)).thenReturn(List.of(existingBooking()));
            List<BookingResponse> responses = bookingService.getBookingsByUserId(1L);
            assertThat(responses).hasSize(1);
        }
    }
}