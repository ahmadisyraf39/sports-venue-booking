package com.ahmadisyraf39.sportsbooking.notification_service.service;

import com.ahmadisyraf39.sportsbooking.notification_service.dto.response.NotificationResponse;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationLog;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationStatus;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentConfirmedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.event.PaymentFailedEvent;
import com.ahmadisyraf39.sportsbooking.notification_service.repository.NotificationLogRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationLog existingNotificationLog() {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setId(1L);
        notificationLog.setRecipientUserId(10L);
        notificationLog.setBookingId("booking-1");
        notificationLog.setType(NotificationType.BOOKING_CONFIRMED);
        notificationLog.setMessage("Your booking has been confirmed!");
        return notificationLog;
    }

        @Nested
    class ProcessPaymentConfirmed {

        private final PaymentConfirmedEvent event =
                new PaymentConfirmedEvent(1L, "booking-1", 10L, new BigDecimal("25.00"));

        @Test
        void shouldSendEmail_AndSaveLogAsSent() {
            notificationService.processPaymentConfirmed(event);

            ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(mailCaptor.capture());
            assertThat(mailCaptor.getValue().getTo()).containsExactly("user10@example.com");
            assertThat(mailCaptor.getValue().getText()).isEqualTo("Your booking booking-1 has been confirmed!");

            ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(notificationLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(logCaptor.getValue().getType()).isEqualTo(NotificationType.BOOKING_CONFIRMED);
            assertThat(logCaptor.getValue().getBookingId()).isEqualTo("booking-1");
            assertThat(logCaptor.getValue().getRecipientUserId()).isEqualTo(10L);
        }

        @Test
        void shouldNotSendAgain_WhenAlreadySent() {
            NotificationLog existing = existingNotificationLog();
            existing.setStatus(NotificationStatus.SENT);
            when(notificationLogRepository.findByBookingIdAndType("booking-1", NotificationType.BOOKING_CONFIRMED))
                    .thenReturn(List.of(existing));

            notificationService.processPaymentConfirmed(event);

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
            verify(notificationLogRepository, never()).save(any(NotificationLog.class));
        }

        @Test
        void shouldRetry_WhenPreviousAttemptFailed() {
            NotificationLog existing = existingNotificationLog();
            existing.setStatus(NotificationStatus.FAILED);
            when(notificationLogRepository.findByBookingIdAndType("booking-1", NotificationType.BOOKING_CONFIRMED))
                    .thenReturn(List.of(existing));

            notificationService.processPaymentConfirmed(event);

            verify(mailSender).send(any(SimpleMailMessage.class));
            assertThat(existing.getStatus()).isEqualTo(NotificationStatus.SENT);
            verify(notificationLogRepository).save(existing);
        }

        @Test
        void shouldMarkFailed_AndRethrow_WhenEmailFails() {
            doThrow(new MailSendException("550 Too many emails per second"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            assertThatThrownBy(() -> notificationService.processPaymentConfirmed(event))
                    .isInstanceOf(MailSendException.class);

            ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(notificationLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        }
    }

    @Nested
    class ProcessPaymentFailed {

        @Test
        void shouldSendEmail_AndSaveLogAsSent() {
            PaymentFailedEvent event = new PaymentFailedEvent(1L, "booking-1", 10L, new BigDecimal("25.00"));

            notificationService.processPaymentFailed(event);

            ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(mailCaptor.capture());
            assertThat(mailCaptor.getValue().getText())
                    .isEqualTo("Your payment for booking booking-1 could not be processed");

            ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(notificationLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
            assertThat(logCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        }
    }

    @Nested
    class GetNotificationsByUserId {

        @Test
        void shouldReturnNotifications_ForUserId() {
            when(notificationLogRepository.findByRecipientUserIdOrderBySentAtDesc(10L))
                    .thenReturn(List.of(existingNotificationLog()));

            List<NotificationResponse> responses = notificationService.getNotificationsByUserId(10L);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getRecipientUserId()).isEqualTo(10L);
        }
    }
}
