package com.gbsw.snapy.domain.friends.controller;

import com.gbsw.snapy.domain.friends.dto.response.FriendResponse;
import com.gbsw.snapy.domain.friends.service.FriendService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getRecommendedFriends(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<FriendResponse> response = friendService.getRecommendedFriends(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{handle}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(
            @PathVariable String handle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        friendService.deleteFriend(principal.getId(), handle);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
