package com.gbsw.snapy.domain.settings.controller;

import com.gbsw.snapy.domain.settings.dto.request.UpdateAlbumVisibilityRequest;
import com.gbsw.snapy.domain.settings.dto.request.UpdateFeedVisibilityRequest;
import com.gbsw.snapy.domain.settings.dto.response.UserSettingResponse;
import com.gbsw.snapy.domain.settings.service.UserSettingService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/settings")
public class UserSettingController {

    private final UserSettingService userSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingResponse>> getSettings(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(userSettingService.getSettings(principal.getId())));
    }

    @PatchMapping("/feed-visibility")
    public ResponseEntity<ApiResponse<Void>> updateFeedVisibility(
            @Valid @RequestBody UpdateFeedVisibilityRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userSettingService.updateFeedVisibility(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/album-visibility")
    public ResponseEntity<ApiResponse<Void>> updateAlbumVisibility(
            @Valid @RequestBody UpdateAlbumVisibilityRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userSettingService.updateAlbumVisibility(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
