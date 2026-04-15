package com.gbsw.snapy.domain.comments.controller;

import com.gbsw.snapy.domain.comments.dto.request.CommentUploadRequest;
import com.gbsw.snapy.domain.comments.dto.response.CommentResponse;
import com.gbsw.snapy.domain.comments.dto.response.CommentUploadResponse;
import com.gbsw.snapy.domain.comments.service.CommentService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.common.CursorResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("albums/{albumId}/comments")
    public ResponseEntity<ApiResponse<CommentUploadResponse>> upload(
            @PathVariable Long albumId,
            @Valid @ModelAttribute CommentUploadRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CommentUploadResponse response = commentService.upload(albumId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("albums/{albumId}/comments")
    public ResponseEntity<ApiResponse<CursorResponse<CommentResponse>>> getComments(
            @PathVariable Long albumId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CursorResponse<CommentResponse> response = commentService.getComments(albumId, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        commentService.delete(commentId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
