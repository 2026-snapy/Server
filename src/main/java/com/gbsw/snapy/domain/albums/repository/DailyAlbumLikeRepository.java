package com.gbsw.snapy.domain.albums.repository;

import com.gbsw.snapy.domain.albums.entity.DailyAlbumLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT l.albumId AS albumId, COUNT(l) AS count " +
            "FROM DailyAlbumLike l WHERE l.albumId IN :albumIds GROUP BY l.albumId")
    List<AlbumLikeCountProjection> countByAlbumIdIn(@Param("albumIds") Collection<Long> albumIds);

    interface AlbumLikeCountProjection {
        Long getAlbumId();
        Long getCount();
    }
}
