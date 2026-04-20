package com.gbsw.snapy.domain.albums.controller;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumListResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumPublishResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumTodayResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumUploadResponse;
import com.gbsw.snapy.domain.albums.service.AlbumCommandService;
import com.gbsw.snapy.domain.albums.service.AlbumQueryService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumCommandService albumCommandService;
    private final AlbumQueryService albumQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumUploadResponse>> upload(
            @Valid @ModelAttribute AlbumUploadRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumUploadResponse response = albumCommandService.upload(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlbumListResponse>>> getAlbumsByMonth(
            @RequestParam int month,
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long targetUserId = (userId != null) ? userId : principal.getId();
        List<AlbumListResponse> response = albumQueryService.getAlbumsByMonth(targetUserId, month, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<AlbumListResponse>>> getCalendarThumbnails(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<AlbumListResponse> response = albumQueryService.getCalendarThumbnails(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AlbumTodayResponse>> getToday(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumTodayResponse response = albumQueryService.getTodayAlbum(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDetailResponse>> getDetail(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumDetailResponse response = albumQueryService.getAlbumDetail(albumId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{albumId}/publish")
    public ResponseEntity<ApiResponse<AlbumPublishResponse>> publish(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumPublishResponse response = albumCommandService.publishAlbum(albumId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
