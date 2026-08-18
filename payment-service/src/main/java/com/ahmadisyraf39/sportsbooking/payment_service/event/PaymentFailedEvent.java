package com.ahmadisyraf39.sportsbooking.payment_service.event;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private Long paymentId;
    private String bookingId;
    private Long userId;
    private BigDecimal paymentTotal;
}
