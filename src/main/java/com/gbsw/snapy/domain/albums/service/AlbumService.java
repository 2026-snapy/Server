package com.gbsw.snapy.domain.albums.service;

import com.gbsw.snapy.domain.albums.dto.request.AlbumUploadRequest;
import com.gbsw.snapy.domain.albums.dto.response.AlbumUploadResponse;
import com.gbsw.snapy.domain.albums.entity.AlbumPhoto;
import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.AlbumPhotoRepository;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.photos.dto.response.PhotoUploadResponse;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.service.PhotoService;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final PhotoService photoService;

    @Transactional
    public AlbumUploadResponse upload(AlbumUploadRequest request, Long userId) {
        PhotoUploadResponse frontPhoto = photoService.upload(request.getFrontImage(), userId);
        PhotoUploadResponse backPhoto = photoService.upload(request.getBackImage(), userId);

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

        album.increasePhotoCount(1);

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

        albumPhotoRepository.deleteAll(albumPhotos);
        dailyAlbumRepository.delete(album);

        for (Long photoId : photoIds) {
            photoService.delete(photoId, userId);
        }
    }
}
