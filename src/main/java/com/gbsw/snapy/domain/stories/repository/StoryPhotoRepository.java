package com.gbsw.snapy.domain.stories.repository;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.stories.entity.StoryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryPhotoRepository extends JpaRepository<StoryPhoto, Long> {

    List<StoryPhoto> findByStoryIdOrderByTypeAsc(Long storyId);

    List<StoryPhoto> findByStoryIdInOrderByTypeAsc(List<Long> storyIds);

    boolean existsByStoryIdAndType(Long storyId, AlbumPhotoType type);
}
