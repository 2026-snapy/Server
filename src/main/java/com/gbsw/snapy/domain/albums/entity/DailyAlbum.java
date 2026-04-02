package com.gbsw.snapy.domain.albums.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_albums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "album_date", nullable = false)
    private LocalDate albumDate;

    @Column(name = "photo_count", nullable = false)
    private int photoCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public DailyAlbum(Long userId, LocalDate albumDate) {
        this.userId = userId;
        this.albumDate = albumDate;
        this.photoCount = 0;
    }

    public void increasePhotoCount(int count) {
        this.photoCount += count;
    }
}
