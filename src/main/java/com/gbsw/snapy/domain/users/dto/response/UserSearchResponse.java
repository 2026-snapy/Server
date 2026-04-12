package com.gbsw.snapy.domain.users.dto.response;

import com.gbsw.snapy.domain.users.entity.User;

public record UserSearchResponse(
        String handle,
        String username,
        String profileImageUrl
) {
    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(user.getHandle(), user.getUsername(), user.getProfileImageUrl());
    }
}
