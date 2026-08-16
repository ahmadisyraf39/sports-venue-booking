package com.ahmadisyraf39.sportsbooking.booking_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;

    private Long userId;
    private Long venueId;
    private Long courtId;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private BookingStatus status;

    private BigDecimal totalPrice;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
