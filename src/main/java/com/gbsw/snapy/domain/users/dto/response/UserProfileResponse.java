package com.gbsw.snapy.domain.users.dto.response;

import com.gbsw.snapy.domain.users.entity.User;

public record UserProfileResponse(
        String handle,
        String username,
        String profileImageUrl,
        String backgroundImageUrl
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getHandle(),
                user.getUsername(),
                user.getProfileImageUrl(),
                user.getBackGroundImageUrl()
        );
    }
}
