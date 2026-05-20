package com.gbsw.snapy.domain.blocks.repository;

import com.gbsw.snapy.domain.blocks.entity.UserBlock;
import com.gbsw.snapy.domain.blocks.entity.UserBlockId;
import com.gbsw.snapy.domain.blocks.repository.projection.BlockedUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {

    boolean existsById_UserIdAndId_TargetUserId(Long userId, Long targetUserId);

    void deleteById_UserIdAndId_TargetUserId(Long userId, Long targetUserId);

    @Query("SELECT COUNT(b) > 0 FROM UserBlock b " +
           "WHERE (b.id.userId = :userA AND b.id.targetUserId = :userB) " +
           "   OR (b.id.userId = :userB AND b.id.targetUserId = :userA)")
    boolean existsBlockBetween(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("SELECT CASE WHEN b.id.userId = :userId THEN b.id.targetUserId ELSE b.id.userId END " +
           "FROM UserBlock b " +
           "WHERE (b.id.userId = :userId AND b.id.targetUserId IN :candidates) " +
           "   OR (b.id.targetUserId = :userId AND b.id.userId IN :candidates)")
    List<Long> findBlockRelatedUserIds(@Param("userId") Long userId,
                                       @Param("candidates") java.util.Collection<Long> candidates);

    @Query("SELECT CASE WHEN b.id.userId = :userId THEN b.id.targetUserId ELSE b.id.userId END " +
           "FROM UserBlock b " +
           "WHERE b.id.userId = :userId OR b.id.targetUserId = :userId")
    List<Long> findAllBlockRelatedUserIds(@Param("userId") Long userId);

    @Query(value = "SELECT u.handle AS handle, u.username AS username, " +
                   "       u.profile_image_url AS profileImageUrl, b.blocked_at AS blockedAt " +
                   "FROM user_blocks b JOIN users u ON u.id = b.target_user_id " +
                   "WHERE b.user_id = :userId AND u.deleted_at IS NULL " +
                   "ORDER BY b.blocked_at DESC", nativeQuery = true)
    List<BlockedUserProjection> findBlockedUsersByUserId(@Param("userId") Long userId);
}
