package com.ahmadisyraf39.sportsbooking.venue_service.dto.response;

import com.ahmadisyraf39.sportsbooking.venue_service.entity.SportType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtResponse {

    private Long id;
    private Long venueId;
    private String name;
    private SportType sportType;
    private BigDecimal hourlyPrice;
}
