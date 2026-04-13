package com.gbsw.snapy.domain.friends.repository.projection;

public interface ReceivedFriendRequestProjection {
    Long getRequestId();
    String getHandle();
    String getUsername();
    String getProfileImageUrl();
}
