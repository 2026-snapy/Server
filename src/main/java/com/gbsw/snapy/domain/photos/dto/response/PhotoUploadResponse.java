package com.gbsw.snapy.domain.photos.dto.response;

import com.gbsw.snapy.domain.photos.entity.Photo;

import java.time.LocalDateTime;

public record PhotoUploadResponse(
        Long photoId,
        String s3Key,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static PhotoUploadResponse from(Photo photo) {
        return new PhotoUploadResponse(
                photo.getId(),
                photo.getS3Key(),
                photo.getImageUrl(),
                photo.getCreatedAt()
        );
    }
}