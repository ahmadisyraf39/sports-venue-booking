package com.ahmadisyraf39.sportsbooking.notification_service.controller;

import com.ahmadisyraf39.sportsbooking.notification_service.dto.response.NotificationResponse;
import com.ahmadisyraf39.sportsbooking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }
}
