package com.gbsw.snapy.domain.users.controller;

import com.gbsw.snapy.domain.friends.dto.response.FriendResponse;
import com.gbsw.snapy.domain.friends.service.FriendService;
import com.gbsw.snapy.domain.guestbook.dto.request.GuestBookCreateRequest;
import com.gbsw.snapy.domain.guestbook.dto.response.GuestBookCreateResponse;
import com.gbsw.snapy.domain.guestbook.dto.response.GuestBookResponse;
import com.gbsw.snapy.domain.guestbook.service.GuestBookService;
import com.gbsw.snapy.domain.users.dto.response.UpdateBackgroundImageResponse;
import com.gbsw.snapy.domain.users.dto.response.UpdateProfileImageResponse;
import com.gbsw.snapy.domain.users.dto.response.UserProfileResponse;
import com.gbsw.snapy.domain.users.dto.response.UserSearchResponse;
import com.gbsw.snapy.domain.users.service.UserService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FriendService friendService;
    private final GuestBookService guestBookService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String q
    ) {
        List<UserSearchResponse> response = userService.searchUsers(q);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

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

    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UpdateProfileImageResponse>> updateProfileImage(
            @RequestParam MultipartFile image,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UpdateProfileImageResponse response = userService.updateProfileImage(principal.getId(), image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{handle}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable String handle
    ) {
        UserProfileResponse response = userService.getProfile(handle);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{handle}/guestbook")
    public ResponseEntity<ApiResponse<GuestBookCreateResponse>> createGuestBook(
            @PathVariable String handle,
            @ModelAttribute GuestBookCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        GuestBookCreateResponse response = guestBookService.create(handle, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{handle}/guestbook")
    public ResponseEntity<ApiResponse<List<GuestBookResponse>>> getGuestBook(
            @PathVariable String handle
    ) {
        List<GuestBookResponse> response = guestBookService.getGuestBook(handle);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{handle}/friends")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
            @PathVariable String handle
    ) {
        List<FriendResponse> response = friendService.getFriends(handle);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{handle}/mutual-friends")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getMutualFriends(
            @PathVariable String handle,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<FriendResponse> response = friendService.getMutualFriends(principal.getId(), handle);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
