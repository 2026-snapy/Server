package com.gbsw.snapy.domain.stories.repository;

import com.gbsw.snapy.domain.stories.entity.StoryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryPhotoRepository extends JpaRepository<StoryPhoto, Long> {
}
