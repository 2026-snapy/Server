package com.gbsw.snapy.domain.notifications.event;

public record GuestbookCreatedEvent(Long guestbookId, Long senderId, Long ownerId) {
}
