package com.gbsw.snapy.domain.blocks.dto.response;

import com.gbsw.snapy.domain.blocks.repository.projection.BlockedUserProjection;

import java.time.LocalDateTime;

public record BlockedUserResponse(
        String handle,
        String username,
        String profileImageUrl,
        LocalDateTime blockedAt
) {
    public static BlockedUserResponse from(BlockedUserProjection projection) {
        return new BlockedUserResponse(
                projection.getHandle(),
                projection.getUsername(),
                projection.getProfileImageUrl(),
                projection.getBlockedAt()
        );
    }
}
