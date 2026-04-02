package com.gbsw.snapy.domain.albums.repository;

import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyAlbumRepository extends JpaRepository<DailyAlbum, Long> {

    Optional<DailyAlbum> findByUserIdAndAlbumDate(Long userId, LocalDate albumDate);

    List<DailyAlbum> findByUserIdAndAlbumDateBetweenOrderByAlbumDateDesc(
            Long userId, LocalDate start, LocalDate end);
}
