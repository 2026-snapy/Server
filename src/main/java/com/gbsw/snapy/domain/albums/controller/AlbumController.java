package com.gbsw.snapy.domain.albums.controller;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumListResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumPublishResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumTodayResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumUploadResponse;
import com.gbsw.snapy.domain.albums.service.AlbumService;
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

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumUploadResponse>> upload(
            @Valid @ModelAttribute AlbumUploadRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumUploadResponse response = albumService.upload(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlbumListResponse>>> getAlbumsByMonth(
            @RequestParam int month,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<AlbumListResponse> response = albumService.getAlbumsByMonth(principal.getId(), month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<AlbumListResponse>>> getCalendarThumbnails(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<AlbumListResponse> response = albumService.getCalendarThumbnails(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AlbumTodayResponse>> getToday(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumTodayResponse response = albumService.getTodayAlbum(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDetailResponse>> getDetail(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumDetailResponse response = albumService.getAlbumDetail(albumId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{albumId}/publish")
    public ResponseEntity<ApiResponse<AlbumPublishResponse>> publish(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AlbumPublishResponse response = albumService.publishAlbum(albumId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
