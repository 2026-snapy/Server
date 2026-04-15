package com.gbsw.snapy.domain.stories.dto.response;

import java.time.LocalDateTime;

public record StoryLikeListResponse(
        Long userId,
        String handle,
        String username,
        String profileImageUrl,
        LocalDateTime likedAt
) {
}
