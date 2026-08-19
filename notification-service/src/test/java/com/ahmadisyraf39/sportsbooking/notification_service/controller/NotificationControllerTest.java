package com.ahmadisyraf39.sportsbooking.notification_service.controller;

import com.ahmadisyraf39.sportsbooking.notification_service.dto.response.NotificationResponse;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import com.ahmadisyraf39.sportsbooking.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponse sampleNotificationResponse() {
        return NotificationResponse.builder()
                .id(1L)
                .recipientUserId(10L)
                .bookingId("booking-1")
                .type(NotificationType.BOOKING_CONFIRMED)
                .message("Your booking has been confirmed!")
                .build();
    }

    @Test
    void shouldReturn200_WhenGettingNotificationsByUserId() throws Exception {
        when(notificationService.getNotificationsByUserId(10L)).thenReturn(List.of(sampleNotificationResponse()));

        mockMvc.perform(get("/api/notifications").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipientUserId").value(10))
                .andExpect(jsonPath("$[0].bookingId").value("booking-1"));
    }

    @Test
    void shouldReturn400_WhenUserIdParamMissing() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isBadRequest());
    }
}
