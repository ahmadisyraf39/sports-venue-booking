package com.ahmadisyraf39.sportsbooking.booking_service.dto.external;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtDetails {
    private Long id;
    private Long venueId;
    private String name;
    private String sportType;
    private BigDecimal hourlyPrice;
}