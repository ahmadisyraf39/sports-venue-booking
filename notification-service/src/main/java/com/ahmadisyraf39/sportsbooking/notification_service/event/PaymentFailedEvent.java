package com.ahmadisyraf39.sportsbooking.notification_service.event;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors payment-service's PaymentFailedEvent field-for-field (same JSON shape),
 * since services do not share a common event library.
 */
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
