package com.gbsw.snapy.domain.photos.dto.response;

import com.gbsw.snapy.domain.photos.entity.Photo;

import java.time.LocalDateTime;

public record PhotoUploadResponse(
        Long photoId,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static PhotoUploadResponse from(Photo photo) {
        return new PhotoUploadResponse(
                photo.getId(),
                photo.getImageUrl(),
                photo.getCreatedAt()
        );
    }
}