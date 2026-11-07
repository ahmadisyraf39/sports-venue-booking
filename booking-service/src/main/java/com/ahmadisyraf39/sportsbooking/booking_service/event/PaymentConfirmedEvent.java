package com.ahmadisyraf39.sportsbooking.booking_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {
    private Long paymentId;
    private String bookingId;
    private Long userId;
    private BigDecimal paymentTotal;
}