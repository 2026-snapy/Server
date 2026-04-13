package com.gbsw.snapy.domain.friend.controller;

import com.gbsw.snapy.domain.friend.service.FriendService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    private final FriendService friendService;

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
