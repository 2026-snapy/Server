package com.gbsw.snapy.domain.friends.controller;

import com.gbsw.snapy.domain.friends.service.FriendService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @DeleteMapping("/{handle}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(
            @PathVariable String handle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        friendService.deleteFriend(principal.getId(), handle);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
