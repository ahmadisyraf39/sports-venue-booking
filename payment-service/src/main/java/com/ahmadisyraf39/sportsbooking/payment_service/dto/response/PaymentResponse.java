package com.ahmadisyraf39.sportsbooking.payment_service.dto.response;

import com.ahmadisyraf39.sportsbooking.payment_service.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private String bookingId;
    private Long venueId;
    private Long userId;

    private BigDecimal paymentTotal;

    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
