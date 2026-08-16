package com.ahmadisyraf39.sportsbooking.venue_service.dto.request;

import com.ahmadisyraf39.sportsbooking.venue_service.entity.SportType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private SportType sportType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal hourlyPrice;

    @NotNull
    private Long ownerId;
}
