package com.gbsw.snapy.domain.friend.repository.projection;

public interface ReceivedFriendRequestProjection {
    Long getRequestId();
    String getHandle();
    String getUsername();
    String getProfileImageUrl();
}
