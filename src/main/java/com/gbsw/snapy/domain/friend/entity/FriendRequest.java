package com.gbsw.snapy.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public FriendRequest(Long senderId, Long receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}
