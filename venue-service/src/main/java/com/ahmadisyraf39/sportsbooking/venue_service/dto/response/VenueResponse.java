package com.ahmadisyraf39.sportsbooking.venue_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueResponse {

    private Long id;
    private String name;
    private String address;
    private String description;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Long ownerId;
}
