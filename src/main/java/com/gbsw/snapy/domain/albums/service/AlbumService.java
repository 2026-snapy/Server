package com.gbsw.snapy.domain.albums.service;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
import com.gbsw.snapy.domain.albums.dto.response.AlbumListResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumTodayResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumUploadResponse;
import com.gbsw.snapy.domain.albums.entity.AlbumPhoto;
import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.AlbumPhotoRepository;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.photos.dto.response.PhotoUploadResponse;
import com.gbsw.snapy.domain.photos.entity.Photo;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.repository.PhotoRepository;
import com.gbsw.snapy.domain.photos.service.PhotoService;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final PhotoService photoService;
    private final PhotoRepository photoRepository;
    private final S3Service s3Service;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Transactional
    public AlbumUploadResponse upload(AlbumUploadRequest request, Long userId) {
        ZonedDateTime nowKst = ZonedDateTime.now(KST_ZONE);
        if (!request.getType().matches(nowKst.getHour())) {
            throw new CustomException(ErrorCode.INVALID_ALBUM_PHOTO_TIME_SLOT);
        }

        LocalDate today = nowKst.toLocalDate();
        DailyAlbum album = dailyAlbumRepository.findByUserIdAndAlbumDate(userId, today)
                .orElseGet(() -> dailyAlbumRepository.save(
                        DailyAlbum.builder()
                                .userId(userId)
                                .albumDate(today)
                                .build()
                ));

        if (albumPhotoRepository.existsByAlbumIdAndType(album.getId(), request.getType())) {
            throw new CustomException(ErrorCode.DUPLICATE_ALBUM_PHOTO_TYPE);
        }

        album.increasePhotoCount(1);

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

        albumPhotoRepository.save(
                AlbumPhoto.builder()
                        .albumId(album.getId())
                        .photoId(frontPhoto.photoId())
                        .type(request.getType())
                        .side(PhotoType.FRONT)
                        .build()
        );

        albumPhotoRepository.save(
                AlbumPhoto.builder()
                        .albumId(album.getId())
                        .photoId(backPhoto.photoId())
                        .type(request.getType())
                        .side(PhotoType.BACK)
                        .build()
        );

        return AlbumUploadResponse.from(album, request.getType());
    }

    @Transactional(readOnly = true)
    public AlbumTodayResponse getTodayAlbum(Long userId) {
        LocalDate today = LocalDate.now(KST_ZONE);
        DailyAlbum album = dailyAlbumRepository.findByUserIdAndAlbumDate(userId, today)
                .orElse(null);
        if (album == null) {
            return null;
        }

        List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByAlbumIdOrderByTypeAsc(album.getId());
        if (albumPhotos.isEmpty()) {
            return AlbumTodayResponse.of(album, List.of());
        }

        List<Long> photoIds = albumPhotos.stream().map(AlbumPhoto::getPhotoId).toList();
        Map<Long, Photo> photoById = new HashMap<>();
        for (Photo photo : photoRepository.findAllById(photoIds)) {
            photoById.put(photo.getId(), photo);
        }
        if (photoById.size() != photoIds.size()) {
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        }

        Map<AlbumPhotoType, String> frontUrls = new EnumMap<>(AlbumPhotoType.class);
        Map<AlbumPhotoType, String> backUrls = new EnumMap<>(AlbumPhotoType.class);
        for (AlbumPhoto ap : albumPhotos) {
            Photo photo = photoById.get(ap.getPhotoId());
            if (ap.getSide() == PhotoType.FRONT) {
                frontUrls.put(ap.getType(), photo.getImageUrl());
            } else {
                backUrls.put(ap.getType(), photo.getImageUrl());
            }
        }

        List<AlbumTodayResponse.AlbumPhotoSet> sets = new ArrayList<>();
        for (AlbumPhotoType type : AlbumPhotoType.values()) {
            if (frontUrls.containsKey(type) || backUrls.containsKey(type)) {
                sets.add(new AlbumTodayResponse.AlbumPhotoSet(
                        type,
                        frontUrls.get(type),
                        backUrls.get(type)
                ));
            }
        }

        return AlbumTodayResponse.of(album, sets);
    }

    @Transactional(readOnly = true)
    public List<AlbumListResponse> getAlbumsByMonth(Long userId, int month) {
        if (month < 1 || month > 12) {
            throw new CustomException(ErrorCode.INVALID_MONTH);
        }

        int year = ZonedDateTime.now(KST_ZONE).getYear();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DailyAlbum> albums = dailyAlbumRepository
                .findByUserIdAndAlbumDateBetweenOrderByAlbumDateDesc(userId, start, end);
        if (albums.isEmpty()) {
            return List.of();
        }

        List<Long> albumIds = albums.stream().map(DailyAlbum::getId).toList();
        List<AlbumPhoto> frontPhotos = albumPhotoRepository
                .findByAlbumIdInAndSide(albumIds, PhotoType.FRONT);

        Map<Long, AlbumPhoto> thumbnailByAlbumId = new HashMap<>();
        for (AlbumPhoto ap : frontPhotos) {
            AlbumPhoto current = thumbnailByAlbumId.get(ap.getAlbumId());
            if (current == null || ap.getType().ordinal() < current.getType().ordinal()) {
                thumbnailByAlbumId.put(ap.getAlbumId(), ap);
            }
        }

        List<Long> photoIds = thumbnailByAlbumId.values().stream()
                .map(AlbumPhoto::getPhotoId)
                .toList();
        Map<Long, Photo> photoById = new HashMap<>();
        if (!photoIds.isEmpty()) {
            for (Photo photo : photoRepository.findAllById(photoIds)) {
                photoById.put(photo.getId(), photo);
            }
            if (photoById.size() != photoIds.size()) {
                throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
            }
        }

        List<AlbumListResponse> result = new ArrayList<>(albums.size());
        for (DailyAlbum album : albums) {
            AlbumPhoto thumbnail = thumbnailByAlbumId.get(album.getId());
            String thumbnailUrl = null;
            if (thumbnail != null) {
                Photo photo = photoById.get(thumbnail.getPhotoId());
                thumbnailUrl = photo.getImageUrl();
            }
            result.add(AlbumListResponse.of(album, thumbnailUrl));
        }
        return result;
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
