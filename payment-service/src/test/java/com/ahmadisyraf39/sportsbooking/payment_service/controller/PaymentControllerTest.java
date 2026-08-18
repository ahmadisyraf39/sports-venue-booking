package com.ahmadisyraf39.sportsbooking.payment_service.controller;

import com.ahmadisyraf39.sportsbooking.payment_service.dto.response.PaymentResponse;
import com.ahmadisyraf39.sportsbooking.payment_service.entity.PaymentStatus;
import com.ahmadisyraf39.sportsbooking.payment_service.exception.PaymentNotFoundException;
import com.ahmadisyraf39.sportsbooking.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentResponse samplePaymentResponse() {
        return PaymentResponse.builder()
                .id(1L)
                .bookingId("booking-1")
                .userId(10L)
                .paymentTotal(new BigDecimal("25.00"))
                .status(PaymentStatus.CONFIRMED)
                .build();
    }

    @Test
    void shouldReturn200_WhenGettingPaymentById() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(samplePaymentResponse());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingId").value("booking-1"));
    }

    @Test
    void shouldReturn404_WhenPaymentNotFound() throws Exception {
        when(paymentService.getPaymentById(99L))
                .thenThrow(new PaymentNotFoundException("Payment not found with id: 99"));

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_WhenGettingPaymentsByBookingId() throws Exception {
        when(paymentService.getPaymentsByBookingId("booking-1")).thenReturn(List.of(samplePaymentResponse()));

        mockMvc.perform(get("/api/payments").param("bookingId", "booking-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value("booking-1"));
    }

    @Test
    void shouldReturn400_WhenBookingIdParamMissing() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isBadRequest());
    }
}
