package com.gbsw.snapy.domain.comments.entity;

import com.gbsw.snapy.domain.audios.entity.Audio;
import com.gbsw.snapy.domain.photos.entity.Photo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;

    @Column(name = "emoji_value")
    private String emojiValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id")
    private Audio audio;

    @Builder
    public CommentAttachment(AttachmentType type, String emojiValue, Photo photo, Audio audio) {
        this.type = type;
        this.emojiValue = emojiValue;
        this.photo = photo;
        this.audio = audio;
    }

    public static CommentAttachment ofEmoji(String emojiValue) {
        return CommentAttachment.builder()
                .type(AttachmentType.EMOJI)
                .emojiValue(emojiValue)
                .build();
    }

    public static CommentAttachment ofImage(Photo photo) {
        return CommentAttachment.builder()
                .type(AttachmentType.IMAGE)
                .photo(photo)
                .build();
    }

    public static CommentAttachment ofAudio(Audio audio) {
        return CommentAttachment.builder()
                .type(AttachmentType.AUDIO)
                .audio(audio)
                .build();
    }
}
