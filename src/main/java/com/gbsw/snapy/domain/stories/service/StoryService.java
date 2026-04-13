package com.gbsw.snapy.domain.stories.service;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.stories.entity.Story;
import com.gbsw.snapy.domain.stories.entity.StoryPhoto;
import com.gbsw.snapy.domain.stories.repository.StoryPhotoRepository;
import com.gbsw.snapy.domain.stories.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryPhotoRepository storyPhotoRepository;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Story createStory(Long userId, Long albumId) {
        LocalDateTime nowKst = LocalDateTime.now(KST_ZONE);

        return storyRepository.save(
                Story.builder()
                        .userId(userId)
                        .albumId(albumId)
                        .expiresAt(nowKst.plusHours(24))
                        .build()
        );
    }

    @Transactional
    public void addPhotos(Long storyId, Long frontPhotoId, Long backPhotoId,
                          AlbumPhotoType type) {
        storyPhotoRepository.save(
                StoryPhoto.builder()
                        .storyId(storyId)
                        .photoId(frontPhotoId)
                        .type(type)
                        .side(PhotoType.FRONT)
                        .build()
        );

        storyPhotoRepository.save(
                StoryPhoto.builder()
                        .storyId(storyId)
                        .photoId(backPhotoId)
                        .type(type)
                        .side(PhotoType.BACK)
                        .build()
        );
    }
}
