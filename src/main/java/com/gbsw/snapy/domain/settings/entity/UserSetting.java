package com.gbsw.snapy.domain.settings.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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
    @Column(name = "past_album_visibility", nullable = false)
    private Visibility pastAlbumVisibility;

    @Column(name = "notification_enabled", nullable = false)
    @ColumnDefault("true")
    private boolean notificationEnabled;

    @Builder
    public UserSetting(Long userId) {
        this.userId = userId;
        this.feedVisibility = Visibility.FRIENDS_ONLY;
        this.pastAlbumVisibility = Visibility.FRIENDS_ONLY;
        this.notificationEnabled = true;
    }
}
