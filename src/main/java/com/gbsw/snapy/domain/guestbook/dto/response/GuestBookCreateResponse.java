package com.gbsw.snapy.domain.guestbook.dto.response;

import com.gbsw.snapy.domain.guestbook.entity.GuestBook;
import com.gbsw.snapy.domain.users.entity.User;

import java.time.LocalDateTime;

public record GuestBookCreateResponse(
        UserInfo owner,
        UserInfo author,
        String imageUrl,
        LocalDateTime createdAt
) {
    public record UserInfo(
            String handle,
            String profileImageUrl
    ) {
        public static UserInfo from(User user) {
            return new UserInfo(user.getHandle(), user.getProfileImageUrl());
        }
    }

    public static GuestBookCreateResponse from(GuestBook guestBook) {
        return new GuestBookCreateResponse(
                UserInfo.from(guestBook.getOwner()),
                UserInfo.from(guestBook.getAuthor()),
                guestBook.getImage(),
                guestBook.getCreatedAt()
        );
    }
}
