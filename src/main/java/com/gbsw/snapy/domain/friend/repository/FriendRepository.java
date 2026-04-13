package com.gbsw.snapy.domain.friend.repository;

import com.gbsw.snapy.domain.friend.entity.Friend;
import com.gbsw.snapy.domain.friend.entity.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {

    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE (f.id.userAId = :userA AND f.id.userBId = :userB) OR (f.id.userAId = :userB AND f.id.userBId = :userA)")
    boolean existsFriendship(@Param("userA") Long userA, @Param("userB") Long userB);
}
