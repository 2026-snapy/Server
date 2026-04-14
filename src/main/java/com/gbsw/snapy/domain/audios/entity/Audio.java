package com.gbsw.snapy.domain.audios.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="audios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Audio(Long userId, String s3Key, String audioUrl) {
        this.userId = userId;
        this.s3Key = s3Key;
        this.audioUrl = audioUrl;
    }
}
