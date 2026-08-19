package com.ahmadisyraf39.sportsbooking.notification_service.repository;

import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationLog;
import com.ahmadisyraf39.sportsbooking.notification_service.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByRecipientUserIdOrderBySentAtDesc(Long recipientUserId);

    List<NotificationLog> findByBookingIdAndType(String bookingId, NotificationType type);

}



