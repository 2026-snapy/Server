package com.gbsw.snapy.domain.friend.repository;

import com.gbsw.snapy.domain.friend.entity.Friend;
import com.gbsw.snapy.domain.friend.entity.FriendId;
import com.gbsw.snapy.domain.friend.repository.projection.FriendUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {

    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE (f.id.userAId = :userA AND f.id.userBId = :userB) OR (f.id.userAId = :userB AND f.id.userBId = :userA)")
    boolean existsFriendship(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query(value = "SELECT u.handle, u.username, u.profile_image_url AS profileImageUrl " +
                   "FROM friends f JOIN users u ON u.id = IF(f.user_a_id = :userId, f.user_b_id, f.user_a_id) " +
                   "WHERE f.user_a_id = :userId OR f.user_b_id = :userId", nativeQuery = true)
    List<FriendUserProjection> findFriendsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT u.handle, u.username, u.profile_image_url AS profileImageUrl " +
                   "FROM friends f1 " +
                   "JOIN friends f2 ON IF(f1.user_a_id = :userAId, f1.user_b_id, f1.user_a_id) = IF(f2.user_a_id = :userBId, f2.user_b_id, f2.user_a_id) " +
                   "JOIN users u ON u.id = IF(f1.user_a_id = :userAId, f1.user_b_id, f1.user_a_id) " +
                   "WHERE (f1.user_a_id = :userAId OR f1.user_b_id = :userAId) " +
                   "AND (f2.user_a_id = :userBId OR f2.user_b_id = :userBId)", nativeQuery = true)
    List<FriendUserProjection> findMutualFriends(@Param("userAId") Long userAId, @Param("userBId") Long userBId);
}
