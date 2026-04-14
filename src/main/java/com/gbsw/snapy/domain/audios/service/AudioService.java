package com.gbsw.snapy.domain.audios.service;

import com.gbsw.snapy.domain.audios.dto.response.AudioUploadResponse;
import com.gbsw.snapy.domain.audios.entity.Audio;
import com.gbsw.snapy.domain.audios.repository.AudioRepository;
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
public class AudioService {
    private final S3Service s3Service;
    private final AudioRepository audioRepository;

    @Transactional
    public AudioUploadResponse upload(MultipartFile file, Long userId) {
        S3Service.S3UploadResult result = s3Service.uploadAudio(file, userId);

        try {
            Audio audio = Audio.builder()
                    .userId(userId)
                    .s3Key(result.s3Key())
                    .audioUrl(result.fileUrl())
                    .build();

            Audio saved = audioRepository.save(audio);

            return AudioUploadResponse.from(saved);
        } catch (Exception e) {
            s3Service.delete(result.s3Key());
            throw e;
        }
    }

    @Transactional
    public void delete(Long audioId, Long userId) {
        Audio audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUDIO_NOT_FOUND));

        if (!audio.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        String s3Key = audio.getS3Key();
        audioRepository.delete(audio);
        audioRepository.flush();
        s3Service.delete(s3Key);
    }
}
