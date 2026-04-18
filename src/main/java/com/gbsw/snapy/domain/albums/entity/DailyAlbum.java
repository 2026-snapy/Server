package com.gbsw.snapy.domain.albums.entity;

import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "daily_albums", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "album_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyAlbum {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final int MAX_SET_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "album_date", nullable = false)
    private LocalDate albumDate;

    @Column(name = "photo_count", nullable = false)
    private int photoCount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'DRAFT'")
    @Column(name = "status", nullable = false, length = 16)
    private AlbumStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AlbumStatus.DRAFT;
        }
    }

    @Builder
    public DailyAlbum(Long userId, LocalDate albumDate) {
        this.userId = userId;
        this.albumDate = albumDate;
        this.photoCount = 0;
        this.status = AlbumStatus.DRAFT;
    }

    public void increasePhotoCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("증가값은 양수여야 합니다.");
        }
        if (this.photoCount + count > MAX_SET_COUNT) {
            throw new CustomException(ErrorCode.ALBUM_DAILY_LIMIT_EXCEEDED);
        }
        this.photoCount += count;
    }

    public void publish() {
        if (this.status == AlbumStatus.PUBLISHED) {
            throw new CustomException(ErrorCode.ALBUM_ALREADY_PUBLISHED);
        }
        this.status = AlbumStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now(KST_ZONE);
    }
}
