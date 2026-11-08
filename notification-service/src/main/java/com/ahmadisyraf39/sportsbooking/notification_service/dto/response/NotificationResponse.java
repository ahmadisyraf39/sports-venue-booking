package com.ahmadisyraf39.sportsbooking.notification_service.dto.response;

import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long recipientUserId;
    private String bookingId;

    private NotificationType type;
    private NotificationStatus status;
    private String message;

    private LocalDateTime sentAt;
}
