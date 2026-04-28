package com.gbsw.snapy.domain.albums.repository;

import com.gbsw.snapy.domain.albums.entity.AlbumStatus;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyAlbumRepository extends JpaRepository<DailyAlbum, Long> {

    Optional<DailyAlbum> findByUserIdAndAlbumDate(Long userId, LocalDate albumDate);

    List<DailyAlbum> findByUserIdAndAlbumDateBetweenOrderByAlbumDateDesc(
            Long userId, LocalDate start, LocalDate end);

    List<DailyAlbum> findByIdIn(List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from DailyAlbum a where a.id = :id")
    Optional<DailyAlbum> findByIdForUpdate(@Param("id") Long id);

    @Query("select a.id from DailyAlbum a where a.status = :status and a.albumDate < :date")
    List<Long> findIdsByStatusAndAlbumDateBefore(
            @Param("status") AlbumStatus status,
            @Param("date") LocalDate date
    );
}
