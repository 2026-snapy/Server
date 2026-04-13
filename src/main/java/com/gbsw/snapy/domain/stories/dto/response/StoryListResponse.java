package com.gbsw.snapy.domain.stories.dto.response;

import java.time.LocalDateTime;

public record StoryListResponse(
        Long storyId,
        String handle,
        String username,
        String profileImageUrl,
        String thumbnailUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}
