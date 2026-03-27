package com.gbsw.messiofcoding.infra.s3;

import com.gbsw.messiofcoding.global.exception.CustomException;
import com.gbsw.messiofcoding.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Uploader s3Uploader;

    @Value("${snapy.cloudfront.url}")
    private String cloudfrontDomain;

    public S3UploadResult upload(MultipartFile file, Long userId) {
        validateImageFile(file);
        String s3Key = generateS3Key(userId, file.getOriginalFilename());

        try {
            s3Uploader.upload(s3Key, file.getInputStream(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        String imageUrl = cloudfrontDomain + "/" + s3Key;
        log.info("S3업로드 완료 - key{}", imageUrl);

        return new S3UploadResult(s3Key, imageUrl);
    }

    public void delete(String s3Key) {
        s3Uploader.delete(s3Key);
        log.info("S3 삭제 완료 - key: {}", s3Key);
    }

    private String generateS3Key(Long userId, String OriginalFileName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(OriginalFileName);

        return String.format("photos/%s/%s/%s.%s", userId, date, uuid, extension);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.IMAGE_EMPTY);
        }

        List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp");
        if(!allowedContentTypes.contains(file.getContentType())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        long MAX_SIZE = 10 * 1024 * 1024L;
        if (file.getSize() > MAX_SIZE) {
            throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }

    private String getExtension(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1))
                .orElse("jpg");
    }

    public record S3UploadResult(String s3Key, String imageUrl) {}
}
