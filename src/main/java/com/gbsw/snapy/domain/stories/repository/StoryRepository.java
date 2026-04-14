package com.gbsw.snapy.domain.stories.repository;

import com.gbsw.snapy.domain.stories.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    Optional<Story> findByUserIdAndAlbumId(Long userId, Long albumId);

    List<Story> findByUserIdInAndExpiresAtAfterOrderByCreatedAtDesc(
            List<Long> userIds, LocalDateTime now);
}
