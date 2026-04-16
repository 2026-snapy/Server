package com.gbsw.snapy.domain.notifications.dto.response;

import org.springframework.data.domain.Slice;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationResponse> items,
        int page,
        int size,
        boolean hasNext
) {
    public static NotificationPageResponse of(Slice<NotificationResponse> slice) {
        return new NotificationPageResponse(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }
}
