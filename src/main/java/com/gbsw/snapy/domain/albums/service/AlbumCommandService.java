package com.gbsw.snapy.domain.albums.service;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
import com.gbsw.snapy.domain.albums.dto.response.AlbumPublishResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumUploadResponse;
import com.gbsw.snapy.domain.albums.entity.AlbumPhoto;
import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.AlbumStatus;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.AlbumPhotoRepository;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.notifications.event.AlbumPublishedEvent;
import com.gbsw.snapy.domain.notifications.event.NewStoryEvent;
import com.gbsw.snapy.domain.photos.dto.response.PhotoUploadResponse;
import com.gbsw.snapy.domain.photos.entity.Photo;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.repository.PhotoRepository;
import com.gbsw.snapy.domain.photos.service.PhotoService;
import com.gbsw.snapy.domain.stories.entity.Story;
import com.gbsw.snapy.domain.stories.repository.StoryPhotoRepository;
import com.gbsw.snapy.domain.stories.repository.StoryRepository;
import com.gbsw.snapy.domain.stories.service.StoryService;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumCommandService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final PhotoService photoService;
    private final PhotoRepository photoRepository;
    private final S3Service s3Service;
    private final StoryService storyService;
    private final StoryRepository storyRepository;
    private final StoryPhotoRepository storyPhotoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Transactional
    public AlbumUploadResponse upload(AlbumUploadRequest request, Long userId) {
        ZonedDateTime nowKst = ZonedDateTime.now(KST_ZONE);
        int hour = nowKst.getHour();

        LocalDate today = nowKst.toLocalDate();
        DailyAlbum album = dailyAlbumRepository.findByUserIdAndAlbumDate(userId, today)
                .orElseGet(() -> dailyAlbumRepository.save(
                        DailyAlbum.builder()
                                .userId(userId)
                                .albumDate(today)
                                .build()
                ));

        album = dailyAlbumRepository.findByIdForUpdate(album.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        boolean publishedAlready = album.getStatus() == AlbumStatus.PUBLISHED;
        Optional<Story> existingStory = storyRepository.findByUserIdAndAlbumId(userId, album.getId());

        AlbumPhotoType resolvedType = request.getType();
        if (!resolvedType.matches(hour)) {
            resolvedType = findAvailableFreeSlot(album.getId(), publishedAlready, existingStory);
        }

        if (publishedAlready && existingStory.isPresent()) {
            if (storyPhotoRepository.existsByStoryIdAndType(existingStory.get().getId(), resolvedType)) {
                throw new CustomException(ErrorCode.DUPLICATE_ALBUM_PHOTO_TYPE);
            }
        } else {
            if (albumPhotoRepository.existsByAlbumIdAndType(album.getId(), resolvedType)) {
                throw new CustomException(ErrorCode.DUPLICATE_ALBUM_PHOTO_TYPE);
            }
        }

        if (!publishedAlready) {
            album.increasePhotoCount(1);
        }

        PhotoUploadResponse frontPhoto = photoService.upload(request.getFrontImage(), userId, PhotoType.FRONT);
        PhotoUploadResponse backPhoto = photoService.upload(request.getBackImage(), userId, PhotoType.BACK);

        List<String> uploadedS3Keys = List.of(frontPhoto.s3Key(), backPhoto.s3Key());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    for (String s3Key : uploadedS3Keys) {
                        try {
                            s3Service.delete(s3Key);
                        } catch (Exception e) {
                            log.warn("트랜잭션 롤백 후 S3 정리 실패 - key: {}", s3Key, e);
                        }
                    }
                }
            }
        });

        if (!publishedAlready) {
            albumPhotoRepository.save(
                    AlbumPhoto.builder()
                            .albumId(album.getId())
                            .photoId(frontPhoto.photoId())
                            .type(resolvedType)
                            .side(PhotoType.FRONT)
                            .build()
            );

            albumPhotoRepository.save(
                    AlbumPhoto.builder()
                            .albumId(album.getId())
                            .photoId(backPhoto.photoId())
                            .type(resolvedType)
                            .side(PhotoType.BACK)
                            .build()
            );
        }

        Story story;
        boolean storyCreated = false;
        if (existingStory.isPresent()) {
            story = existingStory.get();
        } else {
            try {
                story = storyService.createStory(userId, album.getId());
                storyCreated = true;
            } catch (DataIntegrityViolationException e) {
                story = storyRepository.findByUserIdAndAlbumId(userId, album.getId())
                        .orElseThrow(() -> e);
            }
        }
        storyService.addPhotos(story.getId(), frontPhoto.photoId(), backPhoto.photoId(), resolvedType);

        if (storyCreated) {
            eventPublisher.publishEvent(new NewStoryEvent(story.getId(), userId));
        }

        return AlbumUploadResponse.from(album, resolvedType);
    }

    private AlbumPhotoType findAvailableFreeSlot(Long albumId, boolean publishedAlready,
                                                 Optional<Story> existingStory) {
        for (AlbumPhotoType free : List.of(AlbumPhotoType.FREE_1, AlbumPhotoType.FREE_2)) {
            boolean taken = (publishedAlready && existingStory.isPresent())
                    ? storyPhotoRepository.existsByStoryIdAndType(existingStory.get().getId(), free)
                    : albumPhotoRepository.existsByAlbumIdAndType(albumId, free);
            if (!taken) {
                return free;
            }
        }
        throw new CustomException(ErrorCode.INVALID_ALBUM_PHOTO_TIME_SLOT);
    }

    @Transactional
    public AlbumPublishResponse publishAlbum(Long albumId, Long userId) {
        DailyAlbum album = dailyAlbumRepository.findByIdForUpdate(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (!album.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        album.publish();

        eventPublisher.publishEvent(new AlbumPublishedEvent(album.getId(), userId));

        return AlbumPublishResponse.from(album);
    }

    @Transactional(readOnly = true)
    public List<Long> findDraftAlbumIdsBefore(LocalDate date) {
        return dailyAlbumRepository.findIdsByStatusAndAlbumDateBefore(AlbumStatus.DRAFT, date);
    }

    @Transactional
    public void autoPublishOne(Long albumId) {
        DailyAlbum album = dailyAlbumRepository.findByIdForUpdate(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (album.getStatus() == AlbumStatus.PUBLISHED) {
            return;
        }

        album.publish();

        eventPublisher.publishEvent(new AlbumPublishedEvent(album.getId(), album.getUserId()));
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        DailyAlbum album = dailyAlbumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (!album.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByAlbumIdOrderByTypeAsc(album.getId());
        List<Long> photoIds = albumPhotos.stream().map(AlbumPhoto::getPhotoId).toList();

        List<Photo> photos = photoRepository.findAllById(photoIds);
        if (photos.size() != photoIds.size()) {
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        }

        boolean hasUnauthorized = photos.stream()
                .anyMatch(photo -> !photo.getUserId().equals(userId));
        if (hasUnauthorized) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<String> s3Keys = photos.stream().map(Photo::getS3Key).toList();

        albumPhotoRepository.deleteAll(albumPhotos);
        photoRepository.deleteAll(photos);
        dailyAlbumRepository.delete(album);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String s3Key : s3Keys) {
                    try {
                        s3Service.delete(s3Key);
                    } catch (Exception e) {
                        log.warn("커밋 후 S3 삭제 실패 - key: {}", s3Key, e);
                    }
                }
            }
        });
    }
}
