package com.gbsw.snapy.domain.users.controller;

import com.gbsw.snapy.domain.users.dto.response.UpdateBackgroundImageResponse;
import com.gbsw.snapy.domain.users.dto.response.UserProfileResponse;
import com.gbsw.snapy.domain.users.service.UserService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserProfileResponse response = userService.getMyProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me/background-image")
    public ResponseEntity<ApiResponse<UpdateBackgroundImageResponse>> updateBackgroundImage(
            @RequestParam MultipartFile image,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UpdateBackgroundImageResponse response = userService.updateBackgroundImage(principal.getId(), image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{handle}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable String handle
    ) {
        UserProfileResponse response = userService.getProfile(handle);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
