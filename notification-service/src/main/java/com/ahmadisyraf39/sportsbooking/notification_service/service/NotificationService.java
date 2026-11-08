package com.ahmadisyraf39.sportsbooking.notification_service.service;

import com.ahmadisyraf39.sportsbooking.notification_service.dto.response.NotificationResponse;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationLog;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationStatus;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
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
        notifyOnce(event.getUserId(), event.getBookingId(), NotificationType.BOOKING_CONFIRMED,
                "Booking Confirmed",
                "Your booking " + event.getBookingId() + " has been confirmed!");
    }

    public void processPaymentFailed(PaymentFailedEvent event) {
        notifyOnce(event.getUserId(), event.getBookingId(), NotificationType.PAYMENT_FAILED,
                "Payment Failed",
                "Your payment for booking " + event.getBookingId() + " could not be processed");
    }

    private void notifyOnce(Long recipientUserId, String bookingId, NotificationType type,
                            String subject, String message) {
        NotificationLog notificationLog = notificationLogRepository.findByBookingIdAndType(bookingId, type)
                .stream()
                .findFirst()
                .orElseGet(() -> newNotificationLog(recipientUserId, bookingId, type, message));

        if (notificationLog.getStatus() == NotificationStatus.SENT) {
            return;   // duplicate delivery of an event we already handled
        }

        try {
            sendEmail(recipientUserId, subject, message);
            notificationLog.setStatus(NotificationStatus.SENT);
        } catch (MailException ex) {
            notificationLog.setStatus(NotificationStatus.FAILED);
            throw ex;   // let the listener's retry policy try again
        } finally {
            notificationLogRepository.save(notificationLog);
        }
    }

    private NotificationLog newNotificationLog(Long recipientUserId, String bookingId,
                                               NotificationType type, String message) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setRecipientUserId(recipientUserId);
        notificationLog.setBookingId(bookingId);
        notificationLog.setType(type);
        notificationLog.setMessage(message);
        return notificationLog;
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
                .status(notificationLog.getStatus())
                .message(notificationLog.getMessage())
                .sentAt(notificationLog.getSentAt())
                .build();
    }
}
