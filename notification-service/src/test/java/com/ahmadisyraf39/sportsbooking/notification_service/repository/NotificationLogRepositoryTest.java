package com.ahmadisyraf39.sportsbooking.notification_service.repository;

import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationLog;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class NotificationLogRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    private NotificationLog newNotificationLog(Long recipientUserId, NotificationType type) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setRecipientUserId(recipientUserId);
        notificationLog.setBookingId("booking-1");
        notificationLog.setType(type);
        notificationLog.setMessage("Your booking has been confirmed!");
        return notificationLog;
    }

    @Test
    void shouldSaveAndRetrieveNotificationLogById() {
        NotificationLog notificationLog = newNotificationLog(10L, NotificationType.BOOKING_CONFIRMED);

        NotificationLog saved = notificationLogRepository.save(notificationLog);

        assertThat(saved.getId()).isNotNull();
        assertThat(notificationLogRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindNotificationLogsByRecipientUserId() {
        notificationLogRepository.save(newNotificationLog(20L, NotificationType.PAYMENT_FAILED));

        List<NotificationLog> found = notificationLogRepository.findByRecipientUserIdOrderBySentAtDesc(20L);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
    }

    @Test
    void shouldReturnEmptyWhenRecipientUserIdNotFound() {
        List<NotificationLog> found = notificationLogRepository.findByRecipientUserIdOrderBySentAtDesc(999L);

        assertThat(found).isEmpty();
    }
}
