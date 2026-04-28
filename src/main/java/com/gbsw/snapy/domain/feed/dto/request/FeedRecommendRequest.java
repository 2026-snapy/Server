package com.gbsw.snapy.domain.feed.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FeedRecommendRequest(
        @Min(1) Long cursor,
        @Min(1) @Max(100) Integer size
) {
}
