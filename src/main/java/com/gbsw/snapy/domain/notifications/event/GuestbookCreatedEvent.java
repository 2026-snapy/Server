package com.gbsw.snapy.domain.notifications.event;

public record GuestbookCreatedEvent(Long ownerId, Long authorId) {
}
