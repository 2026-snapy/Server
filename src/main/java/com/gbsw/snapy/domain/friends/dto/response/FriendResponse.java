package com.gbsw.snapy.domain.friends.dto.response;

import com.gbsw.snapy.domain.friends.repository.projection.FriendUserProjection;

public record FriendResponse(
        String handle,
        String username,
        String profileImageUrl
) {
    public static FriendResponse from(FriendUserProjection projection) {
        return new FriendResponse(
                projection.getHandle(),
                projection.getUsername(),
                projection.getProfileImageUrl()
        );
    }
}
