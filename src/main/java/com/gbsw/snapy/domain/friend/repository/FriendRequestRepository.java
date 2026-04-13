package com.gbsw.snapy.domain.friend.repository;

import com.gbsw.snapy.domain.friend.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    void deleteBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
