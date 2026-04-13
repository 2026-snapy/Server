package com.gbsw.snapy.domain.friends.dto.response;

import com.gbsw.snapy.domain.friends.entity.FriendRequest;
import com.gbsw.snapy.domain.users.entity.User;

public record ReceivedFriendRequestResponse(
        Long requestId,
        String handle,
        String username,
        String profileImageUrl
) {
    public static ReceivedFriendRequestResponse of(FriendRequest request, User sender) {
        return new ReceivedFriendRequestResponse(
                request.getId(),
                sender.getHandle(),
                sender.getUsername(),
                sender.getProfileImageUrl()
        );
    }
}
