package com.gbsw.snapy.domain.users.dto.response;

import com.gbsw.snapy.domain.albums.dto.response.ProfilePastAlbumResponse;
import com.gbsw.snapy.domain.users.entity.User;

import java.util.List;

public record UserProfileResponse(
        String handle,
        String username,
        String profileImageUrl,
        String backgroundImageUrl,
        long friendCount,
        int currentStreak,
        int maxStreak,
        boolean blocked,
        boolean blockedBy,
        List<ProfilePastAlbumResponse> pastAlbums
) {
    public static UserProfileResponse from(
            User user,
            long friendCount,
            int currentStreak,
            int maxStreak,
            boolean blocked,
            boolean blockedBy,
            List<ProfilePastAlbumResponse> pastAlbums
    ) {
        return new UserProfileResponse(
                user.getHandle(),
                user.getUsername(),
                user.getProfileImageUrl(),
                user.getBackGroundImageUrl(),
                friendCount,
                currentStreak,
                maxStreak,
                blocked,
                blockedBy,
                pastAlbums
        );
    }
}
