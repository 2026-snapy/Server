package com.gbsw.snapy.domain.comments.service;

import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.audios.dto.response.AudioUploadResponse;
import com.gbsw.snapy.domain.audios.entity.Audio;
import com.gbsw.snapy.domain.audios.repository.AudioRepository;
import com.gbsw.snapy.domain.audios.service.AudioService;
import com.gbsw.snapy.domain.blocks.repository.UserBlockRepository;
import com.gbsw.snapy.domain.comments.dto.request.CommentUploadRequest;
import com.gbsw.snapy.domain.comments.dto.response.CommentResponse;
import com.gbsw.snapy.domain.comments.dto.response.CommentUploadResponse;
import com.gbsw.snapy.global.common.CursorResponse;
import com.gbsw.snapy.domain.comments.entity.Comment;
import com.gbsw.snapy.domain.comments.entity.CommentAttachment;
import com.gbsw.snapy.domain.comments.repository.CommentAttachmentRepository;
import com.gbsw.snapy.domain.comments.repository.CommentRepository;
import com.gbsw.snapy.domain.notifications.event.FeedCommentEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final UserBlockRepository userBlockRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentUploadResponse upload(Long albumId, Long userId, CommentUploadRequest request) {
        DailyAlbum album = dailyAlbumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (!album.getUserId().equals(userId)
                && userBlockRepository.existsBlockBetween(userId, album.getUserId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

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

        if (!album.getUserId().equals(userId)) {
            eventPublisher.publishEvent(new FeedCommentEvent(
                    comment.getId(), album.getId(), userId, album.getUserId()));
        }

        return CommentUploadResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public CursorResponse<CommentResponse> getComments(Long albumId, Long cursor, int size, Long viewerId) {
        DailyAlbum album = dailyAlbumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (!album.getUserId().equals(viewerId)
                && userBlockRepository.existsBlockBetween(viewerId, album.getUserId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<Comment> comments = (cursor == null)
                ? commentRepository.findByAlbumIdLatest(albumId, size + 1)
                : commentRepository.findByAlbumIdWithCursor(albumId, cursor, size + 1);

        boolean hasNext = comments.size() > size;
        if (hasNext) {
            comments = comments.subList(0, size);
        }

        // 댓글 중 차단 관계인 사람의 댓글 필터링
        Set<Long> commentUserIds = comments.stream()
                .map(c -> c.getUser().getId())
                .filter(id -> !id.equals(viewerId))
                .collect(Collectors.toSet());
        Set<Long> blockedUserIds = commentUserIds.isEmpty()
                ? Set.of()
                : new HashSet<>(userBlockRepository.findBlockRelatedUserIds(viewerId, commentUserIds));

        List<Comment> visibleComments = comments.stream()
                .filter(c -> !blockedUserIds.contains(c.getUser().getId()))
                .toList();

        List<CommentResponse> content = visibleComments.stream()
                .map(CommentResponse::from)
                .toList();

        Long nextCursor = hasNext ? comments.get(comments.size() - 1).getId() : null;

        return CursorResponse.of(content, nextCursor, hasNext);
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        CommentAttachment attachment = comment.getAttachment();
        Long photoId = (attachment != null && attachment.getPhoto() != null) ? attachment.getPhoto().getId() : null;
        Long audioId = (attachment != null && attachment.getAudio() != null) ? attachment.getAudio().getId() : null;

        commentRepository.delete(comment);
        commentRepository.flush();

        if (attachment != null) {
            commentAttachmentRepository.delete(attachment);
            commentAttachmentRepository.flush();
        }

        if (photoId != null) {
            photoService.delete(photoId, userId);
        }
        if (audioId != null) {
            audioService.delete(audioId, userId);
        }
    }
}
