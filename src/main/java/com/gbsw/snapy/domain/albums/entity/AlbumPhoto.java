package com.gbsw.snapy.domain.albums.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "album_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlbumPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlbumPhotoType type;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public AlbumPhoto(Long albumId, Long photoId, AlbumPhotoType type) {
        this.albumId = albumId;
        this.photoId = photoId;
        this.type = type;
    }
}
