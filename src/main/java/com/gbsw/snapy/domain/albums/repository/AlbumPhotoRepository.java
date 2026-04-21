package com.gbsw.snapy.domain.albums.repository;

import com.gbsw.snapy.domain.albums.entity.AlbumPhoto;
import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, Long> {

    List<AlbumPhoto> findByAlbumIdOrderByTypeAsc(Long albumId);

    List<AlbumPhoto> findByAlbumIdAndCreatedAtLessThanEqualOrderByTypeAsc(Long albumId, LocalDateTime createdAt);

    boolean existsByAlbumIdAndType(Long albumId, AlbumPhotoType type);

    List<AlbumPhoto> findByAlbumIdInAndSide(Collection<Long> albumIds, PhotoType side);
}
