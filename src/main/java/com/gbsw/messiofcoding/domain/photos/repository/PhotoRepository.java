package com.gbsw.messiofcoding.domain.photos.repository;

import com.gbsw.messiofcoding.domain.photos.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
