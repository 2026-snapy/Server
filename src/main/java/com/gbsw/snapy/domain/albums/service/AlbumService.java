package com.gbsw.snapy.domain.albums.service;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final PhotoService photoService;
    private final PhotoRepository photoRepository;
    private final S3Service s3Service;

    @Transactional
    public AlbumUploadResponse upload(AlbumUploadRequest request, Long userId) {
        LocalDate today = LocalDate.now();
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
