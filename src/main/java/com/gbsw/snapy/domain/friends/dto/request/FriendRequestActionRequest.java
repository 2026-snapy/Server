package com.gbsw.snapy.domain.friends.dto.request;

import jakarta.validation.constraints.NotNull;

public record FriendRequestActionRequest(
        @NotNull(message = "action은 필수입니다. (APPROVE, REJECT)")
        Action action
) {
    public enum Action {
        APPROVE, REJECT
    }
}
