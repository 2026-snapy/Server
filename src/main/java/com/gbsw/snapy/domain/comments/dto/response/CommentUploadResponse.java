package com.gbsw.snapy.domain.comments.dto.response;

import com.gbsw.snapy.domain.comments.entity.AttachmentType;
import com.gbsw.snapy.domain.comments.entity.Comment;
import com.gbsw.snapy.domain.comments.entity.CommentAttachment;

import java.time.LocalDateTime;

public record CommentUploadResponse(
        Long commentId,
        Long attachmentId,
        AttachmentType type,
        String emojiValue,
        String imageUrl,
        String audioUrl,
        LocalDateTime createdAt
) {
    public static CommentUploadResponse from(Comment comment) {
        CommentAttachment attachment = comment.getAttachment();

        Long attachmentId = null;
        AttachmentType type = null;
        String emojiValue = null;
        String imageUrl = null;
        String audioUrl = null;

        if (attachment != null) {
            attachmentId = attachment.getId();
            type = attachment.getType();
            emojiValue = attachment.getEmojiValue();
            imageUrl = attachment.getPhoto() != null ? attachment.getPhoto().getImageUrl() : null;
            audioUrl = attachment.getAudio() != null ? attachment.getAudio().getAudioUrl() : null;
        }

        return new CommentUploadResponse(
                comment.getId(),
                attachmentId,
                type,
                emojiValue,
                imageUrl,
                audioUrl,
                comment.getCreatedAt()
        );
    }
}
