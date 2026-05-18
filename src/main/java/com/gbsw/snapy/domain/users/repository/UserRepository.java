package com.gbsw.snapy.domain.users.repository;

import com.gbsw.snapy.domain.auth.entity.OAuthProvider;
import com.gbsw.snapy.domain.friends.repository.projection.RecommendedFriendProjection;
import com.gbsw.snapy.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByHandle(String handle);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);
    Optional<User> findByHandle(String handle);
    Optional<User> findByHandleAndDeletedAtIsNull(String handle);
    Optional<User> findByProviderIdAndProvider(String providerId, OAuthProvider provider);
    List<User> findTop10ByPhoneIn(List<String> phones);

    @Query("select u from User u where (lower(u.handle) like lower(concat('%', :q, '%')) or lower(u.username) like lower(concat('%', :q, '%'))) and u.deletedAt is null")
    List<User> searchActiveUsers(@Param("q") String q);

    @Query("select u.id from User u")
    List<Long> findAllIds();

    @Query(value = "SELECT u.id AS id, u.handle AS handle, u.username AS username, " +
                   "       u.profile_image_url AS profileImageUrl " +
                   "FROM users u " +
                   "WHERE u.deleted_at IS NULL " +
                   "  AND u.id <> :myId " +
                   "  AND u.id NOT IN (:excludeIds) " +
                   "ORDER BY RAND() " +
                   "LIMIT :limit", nativeQuery = true)
    List<RecommendedFriendProjection> findRandomActiveUsers(
            @Param("myId") Long myId,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("limit") int limit
    );
}
