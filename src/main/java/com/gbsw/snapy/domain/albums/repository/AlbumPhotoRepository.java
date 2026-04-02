package com.gbsw.snapy.domain.albums.repository;

import com.gbsw.snapy.domain.albums.entity.AlbumPhoto;
import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, Long> {

    List<AlbumPhoto> findByAlbumIdOrderByTypeAsc(Long albumId);

    boolean existsByAlbumIdAndType(Long albumId, AlbumPhotoType type);
}
