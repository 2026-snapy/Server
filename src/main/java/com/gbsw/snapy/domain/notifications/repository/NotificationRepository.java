package com.gbsw.snapy.domain.notifications.repository;

import com.gbsw.snapy.domain.notifications.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Slice<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndReadFalse(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.receiverId = :receiverId AND n.read = false")
    void markAllAsRead(@Param("receiverId") Long receiverId);
}
