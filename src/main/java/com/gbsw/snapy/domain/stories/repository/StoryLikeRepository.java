package com.gbsw.snapy.domain.stories.repository;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.stories.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {

    Optional<StoryLike> findByStoryIdAndUserIdAndType(Long storyId, Long userId, AlbumPhotoType type);

    List<StoryLike> findByStoryIdAndTypeOrderByCreatedAtDesc(Long storyId, AlbumPhotoType type);

    List<StoryLike> findByStoryIdOrderByCreatedAtDesc(Long storyId);
}
