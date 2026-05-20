package com.gbsw.snapy.domain.blocks.repository;

import com.gbsw.snapy.domain.blocks.entity.UserBlock;
import com.gbsw.snapy.domain.blocks.entity.UserBlockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {

    boolean existsById_UserIdAndId_TargetUserId(Long userId, Long targetUserId);

    void deleteById_UserIdAndId_TargetUserId(Long userId, Long targetUserId);
}
