package com.gbsw.snapy.domain.albums.repository;

import com.gbsw.snapy.domain.albums.entity.DailyAlbumLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyAlbumLikeRepository extends JpaRepository<DailyAlbumLike, Long> {

    Optional<DailyAlbumLike> findByAlbumIdAndUserId(Long albumId, Long userId);

    long countByAlbumId(Long albumId);

    boolean existsByAlbumIdAndUserId(Long albumId, Long userId);

    List<DailyAlbumLike> findByAlbumIdInAndUserId(Collection<Long> albumIds, Long userId);

    List<DailyAlbumLike> findByAlbumIdIn(Collection<Long> albumIds);
}
