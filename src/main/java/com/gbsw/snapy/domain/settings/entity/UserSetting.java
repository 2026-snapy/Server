package com.gbsw.snapy.domain.settings.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feed_visibility", nullable = false)
    private Visibility feedVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "album_visibility", nullable = false)
    private Visibility albumVisibility;

    @Builder
    public UserSetting(Long userId) {
        this.userId = userId;
        this.feedVisibility = Visibility.FRIENDS_ONLY;
        this.albumVisibility = Visibility.FRIENDS_ONLY;
    }
}
