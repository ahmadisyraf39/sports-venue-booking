package com.ahmadisyraf39.sportsbooking.booking_service.dto.response;

import com.ahmadisyraf39.sportsbooking.booking_service.entity.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private String id;
    private Long userId;
    private Long venueId;
    private Long courtId;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private BookingStatus status;

    private BigDecimal totalPrice;

    private Instant createdAt;
    private Instant updatedAt;
}
