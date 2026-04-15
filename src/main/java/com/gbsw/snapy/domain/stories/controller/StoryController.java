package com.gbsw.snapy.domain.stories.controller;

import com.gbsw.snapy.domain.stories.dto.response.StoryDetailResponse;
import com.gbsw.snapy.domain.stories.dto.response.StoryLikeListResponse;
import com.gbsw.snapy.domain.stories.dto.response.StoryLikeResponse;
import com.gbsw.snapy.domain.stories.dto.response.StoryListResponse;
import com.gbsw.snapy.domain.stories.service.StoryService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoryListResponse>>> getStories(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<StoryListResponse> response = storyService.getStories(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{storyId}")
    public ResponseEntity<ApiResponse<StoryDetailResponse>> getStoryDetail(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        StoryDetailResponse response = storyService.getStoryDetail(storyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{storyId}/likes")
    public ResponseEntity<ApiResponse<StoryLikeResponse>> toggleLike(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        StoryLikeResponse response = storyService.toggleLike(storyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{storyId}/likes")
    public ResponseEntity<ApiResponse<List<StoryLikeListResponse>>> getLikes(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<StoryLikeListResponse> response = storyService.getLikes(storyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
