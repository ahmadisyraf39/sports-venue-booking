package com.ahmadisyraf39.sportsbooking.venue_service.controller;

import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.CourtCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueCreateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.request.VenueUpdateRequest;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.CourtResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.dto.response.VenueResponse;
import com.ahmadisyraf39.sportsbooking.venue_service.entity.SportType;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.UnauthorizedOperationException;
import com.ahmadisyraf39.sportsbooking.venue_service.exception.VenueNotFoundException;
import com.ahmadisyraf39.sportsbooking.venue_service.service.CourtService;
import com.ahmadisyraf39.sportsbooking.venue_service.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private CourtService courtService;

    private VenueResponse sampleVenueResponse() {
        return VenueResponse.builder()
                .id(1L)
                .name("Downtown Sports Hall")
                .address("123 Main St")
                .description("A great venue")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(22, 0))
                .ownerId(10L)
                .build();
    }

    @Test
    void shouldReturn200_WhenListingVenues() throws Exception {
        when(venueService.listVenues(null)).thenReturn(List.of(sampleVenueResponse()));

        mockMvc.perform(get("/api/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Downtown Sports Hall"));
    }

    @Test
    void shouldReturn200_WhenGettingVenueById() throws Exception {
        when(venueService.getVenue(1L)).thenReturn(sampleVenueResponse());

        mockMvc.perform(get("/api/venues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Downtown Sports Hall"));
    }

    @Test
    void shouldReturn404_WhenVenueNotFound() throws Exception {
        when(venueService.getVenue(99L)).thenThrow(new VenueNotFoundException("Venue not found with id: 99"));

        mockMvc.perform(get("/api/venues/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_WhenListingCourtsForVenue() throws Exception {
        CourtResponse courtResponse = CourtResponse.builder()
                .id(1L)
                .venueId(1L)
                .name("Court 1")
                .sportType(SportType.BADMINTON)
                .hourlyPrice(new BigDecimal("25.00"))
                .build();

        when(courtService.listCourtsForVenue(1L)).thenReturn(List.of(courtResponse));

        mockMvc.perform(get("/api/venues/1/courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Court 1"));
    }

    @Test
    void shouldReturn201_WhenCreatingVenue() throws Exception {
        VenueCreateRequest request = new VenueCreateRequest();
        request.setName("Downtown Sports Hall");
        request.setAddress("123 Main St");
        request.setOpeningTime(LocalTime.of(8, 0));
        request.setClosingTime(LocalTime.of(22, 0));
        request.setOwnerId(10L);

        when(venueService.createVenue(any(VenueCreateRequest.class))).thenReturn(sampleVenueResponse());

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Downtown Sports Hall"));
    }

    @Test
    void shouldReturn400_WhenCreateVenueRequestIsInvalid() throws Exception {
        VenueCreateRequest request = new VenueCreateRequest();
        request.setName("");

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_WhenUpdatingVenue() throws Exception {
        VenueUpdateRequest request = new VenueUpdateRequest();
        request.setName("Updated Name");
        request.setAddress("456 New St");
        request.setOpeningTime(LocalTime.of(9, 0));
        request.setClosingTime(LocalTime.of(21, 0));
        request.setOwnerId(10L);

        when(venueService.updateVenue(eq(1L), any(VenueUpdateRequest.class))).thenReturn(sampleVenueResponse());

        mockMvc.perform(put("/api/venues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403_WhenUpdateVenueOwnerIdMismatch() throws Exception {
        VenueUpdateRequest request = new VenueUpdateRequest();
        request.setName("Updated Name");
        request.setAddress("456 New St");
        request.setOpeningTime(LocalTime.of(9, 0));
        request.setClosingTime(LocalTime.of(21, 0));
        request.setOwnerId(999L);

        when(venueService.updateVenue(eq(1L), any(VenueUpdateRequest.class)))
                .thenThrow(new UnauthorizedOperationException("You do not have permission to modify this venue"));

        mockMvc.perform(put("/api/venues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn204_WhenDeletingVenue() throws Exception {
        mockMvc.perform(delete("/api/venues/1").param("ownerId", "10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn201_WhenAddingCourt() throws Exception {
        CourtCreateRequest request = new CourtCreateRequest();
        request.setName("Court 1");
        request.setSportType(SportType.BADMINTON);
        request.setHourlyPrice(new BigDecimal("25.00"));
        request.setOwnerId(10L);

        CourtResponse courtResponse = CourtResponse.builder()
                .id(1L)
                .venueId(1L)
                .name("Court 1")
                .sportType(SportType.BADMINTON)
                .hourlyPrice(new BigDecimal("25.00"))
                .build();

        when(courtService.addCourt(eq(1L), any(CourtCreateRequest.class))).thenReturn(courtResponse);

        mockMvc.perform(post("/api/venues/1/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Court 1"));
    }
}
