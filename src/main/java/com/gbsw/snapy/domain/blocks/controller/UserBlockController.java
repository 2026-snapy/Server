package com.gbsw.snapy.domain.blocks.controller;

import com.gbsw.snapy.domain.blocks.service.UserBlockService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blocks")
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/{handle}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable String handle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userBlockService.blockUser(principal.getId(), handle);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{handle}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @PathVariable String handle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userBlockService.unblockUser(principal.getId(), handle);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
