package com.gbsw.snapy.domain.friends.dto.response;

public record FriendRequestStatusResponse(
        Status status
) {
    public enum Status {
        NONE, PENDING, RECEIVED, FRIEND
    }
}
