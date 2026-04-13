package com.gbsw.snapy.domain.stories.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "album_id"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StoryStatus status = StoryStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = StoryStatus.EXPIRED;
    }

    public boolean isExpired() {
        return this.status == StoryStatus.EXPIRED;
    }
}
