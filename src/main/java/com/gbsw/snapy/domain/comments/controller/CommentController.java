package com.gbsw.snapy.domain.comments.controller;

import com.gbsw.snapy.domain.comments.dto.request.CommentUploadRequest;
import com.gbsw.snapy.domain.comments.dto.response.CommentUploadResponse;
import com.gbsw.snapy.domain.comments.service.CommentService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/albums/{albumId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentUploadResponse>> upload(
            @PathVariable Long albumId,
            @Valid @ModelAttribute CommentUploadRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CommentUploadResponse response = commentService.upload(albumId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
