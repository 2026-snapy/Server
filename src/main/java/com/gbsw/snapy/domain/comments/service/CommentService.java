package com.gbsw.snapy.domain.comments.service;

import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.audios.dto.response.AudioUploadResponse;
import com.gbsw.snapy.domain.audios.entity.Audio;
import com.gbsw.snapy.domain.audios.repository.AudioRepository;
import com.gbsw.snapy.domain.audios.service.AudioService;
import com.gbsw.snapy.domain.comments.dto.request.CommentUploadRequest;
import com.gbsw.snapy.domain.comments.dto.response.CommentUploadResponse;
import com.gbsw.snapy.domain.comments.entity.Comment;
import com.gbsw.snapy.domain.comments.entity.CommentAttachment;
import com.gbsw.snapy.domain.comments.repository.CommentAttachmentRepository;
import com.gbsw.snapy.domain.comments.repository.CommentRepository;
import com.gbsw.snapy.domain.photos.dto.response.PhotoUploadResponse;
import com.gbsw.snapy.domain.photos.entity.Photo;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.repository.PhotoRepository;
import com.gbsw.snapy.domain.photos.service.PhotoService;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentAttachmentRepository commentAttachmentRepository;

    private final PhotoService photoService;
    private final AudioService audioService;

    private final UserRepository userRepository;
    private final DailyAlbumRepository dailyAlbumRepository;
    private final PhotoRepository photoRepository;
    private final AudioRepository audioRepository;

    @Transactional
    public CommentUploadResponse upload(Long albumId, Long userId, CommentUploadRequest request) {
        DailyAlbum album = dailyAlbumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        CommentAttachment attachment = switch (request.getType()) {
            case EMOJI -> {
                if (request.getEmojiValue() == null || request.getEmojiValue().isBlank()) {
                    throw new CustomException(ErrorCode.INVALID_COMMENT_ATTACHMENT);
                }
                yield CommentAttachment.ofEmoji(request.getEmojiValue());
            }
            case IMAGE -> {
                if (request.getFile() == null || request.getFile().isEmpty()) {
                    throw new CustomException(ErrorCode.INVALID_COMMENT_ATTACHMENT);
                }
                PhotoUploadResponse response = photoService.upload(request.getFile(), userId, PhotoType.COMMENT);
                Photo photo = photoRepository.findById(response.photoId())
                        .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
                yield CommentAttachment.ofImage(photo);
            }
            case AUDIO -> {
                if (request.getFile() == null || request.getFile().isEmpty()) {
                    throw new CustomException(ErrorCode.INVALID_COMMENT_ATTACHMENT);
                }
                AudioUploadResponse response = audioService.upload(request.getFile(), userId);
                Audio audio = audioRepository.findById(response.audioId())
                        .orElseThrow(() -> new CustomException(ErrorCode.AUDIO_NOT_FOUND));
                yield CommentAttachment.ofAudio(audio);
            }
        };

        CommentAttachment savedAttachment = commentAttachmentRepository.save(attachment);
        Comment comment = commentRepository.save(
                Comment.builder()
                        .album(album)
                        .user(user)
                        .attachment(savedAttachment)
                        .build()
        );

        return CommentUploadResponse.from(comment);
    }
}
