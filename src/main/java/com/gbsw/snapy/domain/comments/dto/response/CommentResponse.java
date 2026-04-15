package com.gbsw.snapy.domain.comments.dto.response;

import com.gbsw.snapy.domain.comments.entity.AttachmentType;
import com.gbsw.snapy.domain.comments.entity.Comment;
import com.gbsw.snapy.domain.comments.entity.CommentAttachment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long userId,
        String handle,
        String profileImageUrl,
        AttachmentType type,
        String emojiValue,
        String imageUrl,
        String audioUrl,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        CommentAttachment attachment = comment.getAttachment();

        AttachmentType type = null;
        String emojiValue = null;
        String imageUrl = null;
        String audioUrl = null;

        if (attachment != null) {
            type = attachment.getType();
            emojiValue = attachment.getEmojiValue();
            imageUrl = attachment.getPhoto() != null ? attachment.getPhoto().getImageUrl() : null;
            audioUrl = attachment.getAudio() != null ? attachment.getAudio().getAudioUrl() : null;
        }

        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getHandle(),
                comment.getUser().getProfileImageUrl(),
                type,
                emojiValue,
                imageUrl,
                audioUrl,
                comment.getCreatedAt()
        );
    }
}
