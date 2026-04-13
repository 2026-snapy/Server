package com.gbsw.snapy.domain.friend.controller;

import com.gbsw.snapy.domain.friend.dto.request.FriendRequestActionRequest;
import com.gbsw.snapy.domain.friend.dto.response.FriendRequestStatusResponse;
import com.gbsw.snapy.domain.friend.dto.response.ReceivedFriendRequestResponse;
import com.gbsw.snapy.domain.friend.service.FriendService;
import com.gbsw.snapy.global.common.ApiResponse;
import jakarta.validation.Valid;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    private final FriendService friendService;

    @PatchMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> processRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody FriendRequestActionRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        friendService.processRequest(principal.getId(), requestId, request.action());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ReceivedFriendRequestResponse>>> getReceivedRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ReceivedFriendRequestResponse> response = friendService.getReceivedRequests(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{receiverHandle}")
    public ResponseEntity<ApiResponse<FriendRequestStatusResponse>> getStatus(
            @PathVariable String receiverHandle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        FriendRequestStatusResponse response = friendService.getRequestStatus(principal.getId(), receiverHandle);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{receiverHandle}")
    public ResponseEntity<ApiResponse<Void>> sendRequest(
            @PathVariable String receiverHandle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        friendService.sendRequest(principal.getId(), receiverHandle);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{receiverHandle}")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
            @PathVariable String receiverHandle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        friendService.cancelRequest(principal.getId(), receiverHandle);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
