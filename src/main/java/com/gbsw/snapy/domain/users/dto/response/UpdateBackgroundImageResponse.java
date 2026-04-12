package com.gbsw.snapy.domain.users.dto.response;

import com.gbsw.snapy.domain.users.entity.User;

public record UpdateBackgroundImageResponse(
        String backgroundImageUrl
) {
    public static UpdateBackgroundImageResponse from(User user) {
        return new UpdateBackgroundImageResponse(user.getBackGroundImageUrl());
    }
}
