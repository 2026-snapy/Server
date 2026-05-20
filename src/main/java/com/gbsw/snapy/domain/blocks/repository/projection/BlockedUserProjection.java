package com.gbsw.snapy.domain.blocks.repository.projection;

import java.time.LocalDateTime;

public interface BlockedUserProjection {
    String getHandle();
    String getUsername();
    String getProfileImageUrl();
    LocalDateTime getBlockedAt();
}
