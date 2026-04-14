package com.gbsw.snapy.domain.photos.service;

import com.gbsw.snapy.domain.photos.dto.response.PhotoUploadResponse;
import com.gbsw.snapy.domain.photos.entity.Photo;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.repository.PhotoRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {
    private final S3Service s3Service;
    private final PhotoRepository photoRepository;

    @Transactional
    public PhotoUploadResponse upload(MultipartFile file, Long userId, PhotoType type) {
        S3Service.S3UploadResult result = s3Service.uploadImage(file, userId);

        try {
            Photo photo = Photo.builder()
                    .userId(userId)
                    .s3Key(result.s3Key())
                    .imageUrl(result.fileUrl())
                    .type(type)
                    .build();

            Photo saved = photoRepository.save(photo);

            return PhotoUploadResponse.from(saved);
        } catch (Exception e) {
            s3Service.delete(result.s3Key());
            throw e;
        }
    }

    @Transactional
    public void delete(Long photoId, Long userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

        if (!photo.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        String s3Key = photo.getS3Key();
        photoRepository.delete(photo);
        photoRepository.flush();
        s3Service.delete(s3Key);
    }
}
