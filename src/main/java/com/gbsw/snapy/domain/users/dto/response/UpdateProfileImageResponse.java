package com.gbsw.snapy.domain.users.dto.response;

import com.gbsw.snapy.domain.users.entity.User;

public record UpdateProfileImageResponse(
        String profileImageUrl
) {
    public static UpdateProfileImageResponse from(User user) {
        return new UpdateProfileImageResponse(user.getProfileImageUrl());
    }
}
