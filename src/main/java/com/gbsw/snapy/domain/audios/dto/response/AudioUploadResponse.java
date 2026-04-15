package com.gbsw.snapy.domain.audios.dto.response;

import com.gbsw.snapy.domain.audios.entity.Audio;

import java.time.LocalDateTime;

public record AudioUploadResponse(
        Long audioId,
        String s3Key,
        String audioUrl,
        LocalDateTime createdAt
) {
    public static AudioUploadResponse from(Audio audio) {
        return new AudioUploadResponse(
                audio.getId(),
                audio.getS3Key(),
                audio.getAudioUrl(),
                audio.getCreatedAt()
        );
    }
}
