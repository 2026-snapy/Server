package com.gbsw.snapy.infra.s3;

import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Uploader s3Uploader;

    @Value("${snapy.cloudfront.url}")
    private String cloudfrontDomain;

    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    public S3UploadResult uploadImage(MultipartFile file, Long userId) {
        validateImageFile(file);

        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String s3Key = generateImageS3Key(userId, file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            s3Uploader.upload(s3Key, inputStream, file.getContentType(), file.getSize());
        } catch (Exception e) {
            log.error("S3 업로드 실패 - key: {}, cause: {}", s3Key, e.getMessage(), e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        String fileUrl = cloudfrontDomain + "/" + s3Key;
        log.info("S3업로드 완료 - key: {}", fileUrl);

        return new S3UploadResult(s3Key, fileUrl);
    }

    public S3UploadResult uploadAudio(MultipartFile file, Long userId) {
        validateAudioFile(file);

        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String s3Key = generateAudioS3Key(userId);

        try (InputStream inputStream = file.getInputStream()) {
            s3Uploader.upload(s3Key, inputStream, "audio/mp4", file.getSize());
        } catch (Exception e) {
            log.error("S3 업로드 실패 - key: {}, cause: {}", s3Key, e.getMessage(), e);
            throw new CustomException(ErrorCode.AUDIO_UPLOAD_FAILED);
        }

        String fileUrl = cloudfrontDomain + "/" + s3Key;
        log.info("S3업로드 완료 - key: {}", fileUrl);

        return new S3UploadResult(s3Key, fileUrl);
    }

    public void delete(String s3Key) {
        try {
            s3Uploader.delete(s3Key);
            log.info("S3 삭제 완료 - key: {}", s3Key);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private String generateImageS3Key(Long userId, String contentType) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String uuid = UUID.randomUUID().toString();
        String extension = CONTENT_TYPE_TO_EXTENSION.getOrDefault(contentType, "jpg");

        return String.format("photos/%s/%s/%s.%s", userId, date, uuid, extension);
    }

    private String generateAudioS3Key(Long userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String uuid = UUID.randomUUID().toString();

        return String.format("audios/%s/%s/%s.m4a", userId, date, uuid);
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.AUDIO_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".m4a")) {
            throw new CustomException(ErrorCode.INVALID_AUDIO_TYPE);
        }

        long MAX_SIZE = 20 * 1024 * 1024L;
        if (file.getSize() > MAX_SIZE) {
            throw new CustomException(ErrorCode.AUDIO_SIZE_EXCEEDED);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.IMAGE_EMPTY);
        }

        List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp");
        if(!allowedContentTypes.contains(file.getContentType())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        long MAX_SIZE = 20 * 1024 * 1024L;
        if (file.getSize() > MAX_SIZE) {
            throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }

    public record S3UploadResult(String s3Key, String fileUrl) {}
}
