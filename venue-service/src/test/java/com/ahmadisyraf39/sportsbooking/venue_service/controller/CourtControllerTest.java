package com.ahmadisyraf39.sportsbooking.venue_service.controller;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.CourtResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.SportType;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.CourtNotFoundException;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.UnauthorizedOperationException;
import com.ahmadisyraf39.sportsbooking.venue_service.service.CourtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtController.class)
class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CourtService courtService;

    @Test
    void shouldReturn200_WhenUpdatingCourt() throws Exception {
        CourtUpdateRequest request = new CourtUpdateRequest();
        request.setName("Updated Court");
        request.setSportType(SportType.TENNIS);
        request.setHourlyPrice(new BigDecimal("30.00"));
        request.setOwnerId(10L);

        CourtResponse response = CourtResponse.builder()
                .id(1L)
                .venueId(1L)
                .name("Updated Court")
                .sportType(SportType.TENNIS)
                .hourlyPrice(new BigDecimal("30.00"))
                .build();

        when(courtService.updateCourt(eq(1L), any(CourtUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/courts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Court"));
    }

    @Test
    void shouldReturn404_WhenCourtNotFound() throws Exception {
        CourtUpdateRequest request = new CourtUpdateRequest();
        request.setName("Updated Court");
        request.setSportType(SportType.TENNIS);
        request.setHourlyPrice(new BigDecimal("30.00"));
        request.setOwnerId(10L);

        when(courtService.updateCourt(eq(99L), any(CourtUpdateRequest.class)))
                .thenThrow(new CourtNotFoundException("Court not found with id: 99"));

        mockMvc.perform(put("/api/courts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_WhenUpdateCourtOwnerIdMismatch() throws Exception {
        CourtUpdateRequest request = new CourtUpdateRequest();
        request.setName("Updated Court");
        request.setSportType(SportType.TENNIS);
        request.setHourlyPrice(new BigDecimal("30.00"));
        request.setOwnerId(999L);

        when(courtService.updateCourt(eq(1L), any(CourtUpdateRequest.class)))
                .thenThrow(new UnauthorizedOperationException("You do not have permission to modify this court"));

        mockMvc.perform(put("/api/courts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn204_WhenDeletingCourt() throws Exception {
        mockMvc.perform(delete("/api/courts/1").param("ownerId", "10"))
                .andExpect(status().isNoContent());
    }
}
