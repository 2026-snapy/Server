package com.gbsw.snapy.domain.notifications.repository;

import com.gbsw.snapy.domain.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    long countByReceiverIdAndReadFalse(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.receiverId = :receiverId AND n.read = false")
    void markAllAsRead(Long receiverId);
}
