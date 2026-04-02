package com.gbsw.snapy.domain.albums.controller;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
import com.gbsw.snapy.domain.albums.dto.response.AlbumUploadResponse;
import com.gbsw.snapy.domain.albums.service.AlbumService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
