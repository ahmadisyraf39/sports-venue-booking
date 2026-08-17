package com.ahmadisyraf39.sportsbooking.booking_service.controller;

import com.ahmadisyraf39.sportsbooking.booking_service.dto.response.BookingResponse;
import com.ahmadisyraf39.sportsbooking.booking_service.entity.BookingStatus;
import com.ahmadisyraf39.sportsbooking.booking_service.exception.BookingNotFoundException;
import com.ahmadisyraf39.sportsbooking.booking_service.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private BookingResponse sampleBookingResponse() {
        return BookingResponse.builder()
                .id("booking-1")
                .userId(1L)
                .venueId(2L)
                .courtId(3L)
                .bookingDate(LocalDate.of(2026, 8, 20))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(BookingStatus.PENDING)
                .totalPrice(new BigDecimal("25.00"))
                .build();
    }

    @Test
    void shouldReturn200_WhenGettingBookingById() throws Exception {
        when(bookingService.getBookingById("booking-1")).thenReturn(sampleBookingResponse());

        mockMvc.perform(get("/api/bookings/booking-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("booking-1"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void shouldReturn404_WhenBookingNotFound() throws Exception {
        when(bookingService.getBookingById("missing"))
                .thenThrow(new BookingNotFoundException("Booking not found with id: missing"));

        mockMvc.perform(get("/api/bookings/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_WhenGettingBookingsByUserId() throws Exception {
        when(bookingService.getBookingsByUserId(1L)).thenReturn(List.of(sampleBookingResponse()));

        mockMvc.perform(get("/api/bookings").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("booking-1"));
    }

    @Test
    void shouldReturn400_WhenUserIdParamMissing() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isBadRequest());
    }
}
