package com.gbsw.snapy.domain.notifications.event;

public record FriendRequestEvent(Long requestId, Long senderId, Long receiverId) {
}
