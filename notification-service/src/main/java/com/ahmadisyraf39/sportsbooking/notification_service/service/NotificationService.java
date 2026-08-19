package com.ahmadisyraf39.sportsbooking.notification_service.service;

import com.ahmadisyraf39.sportsbooking.notification_service.dto.response.NotificationResponse;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationLog;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String FROM_ADDRESS = "noreply@sportsbooking.com";

    private final NotificationLogRepository notificationLogRepository;
    private final JavaMailSender mailSender;

    public void processPaymentConfirmed(PaymentConfirmedEvent event) {
        if (alreadyNotified(event.getBookingId(), NotificationType.BOOKING_CONFIRMED)) {
            return;
        }

        String message = "Your booking has been confirmed!";
        saveNotificationLog(event.getUserId(), event.getBookingId(), NotificationType.BOOKING_CONFIRMED, message);
        sendEmail(event.getUserId(), "Booking Confirmed", message);
    }

    public void processPaymentFailed(PaymentFailedEvent event) {
        if (alreadyNotified(event.getBookingId(), NotificationType.PAYMENT_FAILED)) {
            return;
        }

        String message = "Your payment could not be processed";
        saveNotificationLog(event.getUserId(), event.getBookingId(), NotificationType.PAYMENT_FAILED, message);
        sendEmail(event.getUserId(), "Payment Failed", message);
    }

    private boolean alreadyNotified(String bookingId, NotificationType type) {
        return !notificationLogRepository.findByBookingIdAndType(bookingId, type).isEmpty();
    }

    private void saveNotificationLog(Long recipientUserId, String bookingId, NotificationType type, String message) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setRecipientUserId(recipientUserId);
        notificationLog.setBookingId(bookingId);
        notificationLog.setType(type);
        notificationLog.setMessage(message);

        notificationLogRepository.save(notificationLog);
    }

    private void sendEmail(Long recipientUserId, String subject, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(FROM_ADDRESS);
        mailMessage.setTo(resolveRecipientEmail(recipientUserId));
        mailMessage.setSubject(subject);
        mailMessage.setText(body);

        mailSender.send(mailMessage);
    }

    private String resolveRecipientEmail(Long recipientUserId) {
        // Known simplification: real user emails aren't wired through from user-service
        // yet, so recipient addresses are derived deterministically from the user id.
        return "user" + recipientUserId + "@example.com";
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationLogRepository.findByRecipientUserIdOrderBySentAtDesc(userId).stream()
                .map(this::toNotificationResponse)
                .toList();
    }

    private NotificationResponse toNotificationResponse(NotificationLog notificationLog) {
        return NotificationResponse.builder()
                .id(notificationLog.getId())
                .recipientUserId(notificationLog.getRecipientUserId())
                .bookingId(notificationLog.getBookingId())
                .type(notificationLog.getType())
                .message(notificationLog.getMessage())
                .sentAt(notificationLog.getSentAt())
                .build();
    }
}
