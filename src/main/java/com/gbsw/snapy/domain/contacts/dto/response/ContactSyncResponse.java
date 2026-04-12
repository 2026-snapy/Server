package com.gbsw.snapy.domain.contacts.dto.response;

import com.gbsw.snapy.domain.users.entity.User;

import java.util.List;

public record ContactSyncResponse(
        List<ContactUserResponse> contacts
) {
    public record ContactUserResponse(
            String handle,
            String profileImageUrl
    ) {
        public static ContactUserResponse from(User user) {
            return new ContactUserResponse(user.getHandle(), user.getProfileImageUrl());
        }
    }
}
