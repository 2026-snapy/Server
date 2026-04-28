package com.gbsw.snapy.domain.feed.controller;

import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.feed.dto.request.FeedRecommendRequest;
import com.gbsw.snapy.domain.feed.service.FeedService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.common.CursorResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {
    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<AlbumDetailResponse>>> recommend(
            @Valid FeedRecommendRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
            ) {
        return ResponseEntity.ok(ApiResponse.success(feedService.recommend(principal.getId(), request)));
    }
}
