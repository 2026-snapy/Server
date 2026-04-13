package com.gbsw.snapy.domain.stories.entity;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_photos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"story_id", "type", "side"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlbumPhotoType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhotoType side;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
