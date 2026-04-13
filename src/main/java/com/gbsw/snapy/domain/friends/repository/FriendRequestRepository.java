package com.gbsw.snapy.domain.friends.repository;

import com.gbsw.snapy.domain.friends.entity.FriendRequest;
import com.gbsw.snapy.domain.friends.repository.projection.ReceivedFriendRequestProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    void deleteBySenderIdAndReceiverId(Long senderId, Long receiverId);

    @Query(value = "SELECT fr.id AS requestId, u.handle, u.username, u.profile_image_url AS profileImageUrl " +
                   "FROM friend_requests fr JOIN users u ON fr.sender_id = u.id " +
                   "WHERE fr.receiver_id = :receiverId", nativeQuery = true)
    List<ReceivedFriendRequestProjection> findReceivedRequests(@Param("receiverId") Long receiverId);
}
