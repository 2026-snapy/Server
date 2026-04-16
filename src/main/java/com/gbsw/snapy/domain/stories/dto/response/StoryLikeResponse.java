package com.gbsw.snapy.domain.stories.dto.response;

public record StoryLikeResponse(
        Long storyId,
        boolean liked
) {
}
