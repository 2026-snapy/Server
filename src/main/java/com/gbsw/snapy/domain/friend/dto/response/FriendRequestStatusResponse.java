package com.gbsw.snapy.domain.friend.dto.response;

public record FriendRequestStatusResponse(
        Status status
) {
    public enum Status {
        NONE, PENDING, RECEIVED, FRIEND
    }
}
