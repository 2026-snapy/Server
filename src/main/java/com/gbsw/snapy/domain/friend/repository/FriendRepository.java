package com.gbsw.snapy.domain.friend.repository;

import com.gbsw.snapy.domain.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
}
