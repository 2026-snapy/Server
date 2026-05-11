package com.gbsw.snapy.domain.streaks.repository;

import com.gbsw.snapy.domain.streaks.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByUserIdAndActiveTrue(Long userId);

    @Query("select coalesce(max(s.count), 0) from Streak s where s.userId = :userId")
    int findMaxCountByUserId(@Param("userId") Long userId);
}
