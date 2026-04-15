package com.gbsw.snapy.domain.stories.repository;

import com.gbsw.snapy.domain.stories.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {

    Optional<StoryLike> findByStoryIdAndUserId(Long storyId, Long userId);

    long countByStoryId(Long storyId);

    boolean existsByStoryIdAndUserId(Long storyId, Long userId);

    List<StoryLike> findByStoryIdOrderByCreatedAtDesc(Long storyId);
}
