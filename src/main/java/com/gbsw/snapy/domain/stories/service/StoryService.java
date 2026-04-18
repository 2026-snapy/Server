package com.gbsw.snapy.domain.stories.service;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.photos.entity.Photo;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.repository.PhotoRepository;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.entity.Visibility;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.domain.stories.dto.response.StoryDetailResponse;
import com.gbsw.snapy.domain.stories.dto.response.StoryLikeListResponse;
import com.gbsw.snapy.domain.stories.dto.response.StoryLikeResponse;
import com.gbsw.snapy.domain.stories.dto.response.StoryListResponse;
import com.gbsw.snapy.domain.stories.entity.Story;
import com.gbsw.snapy.domain.stories.entity.StoryLike;
import com.gbsw.snapy.domain.stories.entity.StoryPhoto;
import com.gbsw.snapy.domain.stories.repository.StoryLikeRepository;
import com.gbsw.snapy.domain.stories.repository.StoryPhotoRepository;
import com.gbsw.snapy.domain.stories.repository.StoryRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.domain.notifications.event.StoryLikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryPhotoRepository storyPhotoRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final UserSettingRepository userSettingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Story createStory(Long userId, Long albumId) {
        LocalDateTime nowKst = LocalDateTime.now(KST_ZONE);

        return storyRepository.save(
                Story.builder()
                        .userId(userId)
                        .albumId(albumId)
                        .expiresAt(nowKst.plusHours(24))
                        .build()
        );
    }

    @Transactional
    public void addPhotos(Long storyId, Long frontPhotoId, Long backPhotoId,
                          AlbumPhotoType type) {
        storyPhotoRepository.save(
                StoryPhoto.builder()
                        .storyId(storyId)
                        .photoId(frontPhotoId)
                        .type(type)
                        .side(PhotoType.FRONT)
                        .build()
        );

        storyPhotoRepository.save(
                StoryPhoto.builder()
                        .storyId(storyId)
                        .photoId(backPhotoId)
                        .type(type)
                        .side(PhotoType.BACK)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public List<StoryListResponse> getStories(Long userId) {
        List<Long> friendIds = friendRepository.findFriendIdsByUserId(userId);
        if (friendIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime nowKst = LocalDateTime.now(KST_ZONE);
        List<Story> stories = storyRepository
                .findByUserIdInAndExpiresAtAfterOrderByCreatedAtDesc(friendIds, nowKst);

        if (stories.isEmpty()) {
            return List.of();
        }

        List<Long> storyUserIds = stories.stream().map(Story::getUserId).distinct().toList();

        Map<Long, Visibility> visibilityMap = userSettingRepository.findAllById(storyUserIds).stream()
                .collect(Collectors.toMap(UserSetting::getUserId, UserSetting::getFeedVisibility));

        stories = stories.stream()
                .filter(story -> visibilityMap.getOrDefault(story.getUserId(), Visibility.FRIENDS_ONLY) != Visibility.ONLY_ME)
                .toList();

        if (stories.isEmpty()) {
            return List.of();
        }

        Map<Long, User> userMap = userRepository.findAllById(storyUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> storyIds = stories.stream().map(Story::getId).toList();
        List<StoryPhoto> allPhotos = storyPhotoRepository.findByStoryIdInOrderByTypeAsc(storyIds);

        Map<Long, List<StoryPhoto>> photosByStoryId = allPhotos.stream()
                .collect(Collectors.groupingBy(StoryPhoto::getStoryId));

        Map<Long, Photo> photoMap = photoRepository.findAllById(
                allPhotos.stream()
                        .filter(sp -> sp.getSide() == PhotoType.FRONT)
                        .map(StoryPhoto::getPhotoId)
                        .toList()
        ).stream().collect(Collectors.toMap(Photo::getId, Function.identity()));

        List<StoryListResponse> result = new ArrayList<>();
        for (Story story : stories) {
            User user = userMap.get(story.getUserId());
            if (user == null) continue;

            List<StoryPhoto> storyPhotos = photosByStoryId.getOrDefault(story.getId(), List.of());
            StoryPhoto firstFront = storyPhotos.stream()
                    .filter(sp -> sp.getSide() == PhotoType.FRONT)
                    .findFirst()
                    .orElse(null);

            String thumbnailUrl = null;
            if (firstFront != null) {
                Photo photo = photoMap.get(firstFront.getPhotoId());
                if (photo != null) {
                    thumbnailUrl = photo.getImageUrl();
                }
            }

            result.add(new StoryListResponse(
                    story.getId(),
                    user.getHandle(),
                    user.getUsername(),
                    user.getProfileImageUrl(),
                    thumbnailUrl,
                    story.getCreatedAt(),
                    story.getExpiresAt()
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public StoryDetailResponse getStoryDetail(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_NOT_FOUND));

        LocalDateTime nowKst = LocalDateTime.now(KST_ZONE);
        if (story.isExpired(nowKst)) {
            throw new CustomException(ErrorCode.STORY_EXPIRED);
        }

        Long ownerId = story.getUserId();
        if (!ownerId.equals(userId)) {
            Visibility feedVisibility = userSettingRepository.findById(ownerId)
                    .map(UserSetting::getFeedVisibility)
                    .orElse(Visibility.FRIENDS_ONLY);

            if (feedVisibility == Visibility.ONLY_ME) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            if (feedVisibility == Visibility.FRIENDS_ONLY) {
                boolean isFriend = friendRepository.existsFriendship(userId, ownerId);
                if (!isFriend) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
            }
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<StoryPhoto> storyPhotos = storyPhotoRepository.findByStoryIdOrderByTypeAsc(storyId);

        List<Long> photoIds = storyPhotos.stream().map(StoryPhoto::getPhotoId).toList();
        Map<Long, Photo> photoMap = photoRepository.findAllById(photoIds).stream()
                .collect(Collectors.toMap(Photo::getId, Function.identity()));

        Map<AlbumPhotoType, List<StoryPhoto>> groupedByType = storyPhotos.stream()
                .collect(Collectors.groupingBy(StoryPhoto::getType));

        List<StoryDetailResponse.StoryPhotoSet> photoSets = new ArrayList<>();
        for (Map.Entry<AlbumPhotoType, List<StoryPhoto>> entry : groupedByType.entrySet()) {
            String frontUrl = null;
            String backUrl = null;
            LocalDateTime photoCreatedAt = null;

            for (StoryPhoto sp : entry.getValue()) {
                Photo photo = photoMap.get(sp.getPhotoId());
                if (photo == null) continue;

                if (sp.getSide() == PhotoType.FRONT) {
                    frontUrl = photo.getImageUrl();
                    photoCreatedAt = sp.getCreatedAt();
                } else {
                    backUrl = photo.getImageUrl();
                }
            }

            photoSets.add(new StoryDetailResponse.StoryPhotoSet(
                    entry.getKey(), frontUrl, backUrl, photoCreatedAt));
        }

        photoSets.sort((a, b) -> a.type().compareTo(b.type()));

        return new StoryDetailResponse(
                story.getId(),
                owner.getHandle(),
                owner.getUsername(),
                owner.getProfileImageUrl(),
                photoSets,
                story.getCreatedAt(),
                story.getExpiresAt()
        );
    }

    @Transactional
    public StoryLikeResponse toggleLike(Long storyId, AlbumPhotoType type, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_NOT_FOUND));

        LocalDateTime nowKst = LocalDateTime.now(KST_ZONE);
        if (story.isExpired(nowKst)) {
            throw new CustomException(ErrorCode.STORY_EXPIRED);
        }

        Long ownerId = story.getUserId();
        if (ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.CANNOT_LIKE_OWN_STORY);
        }

        Visibility feedVisibility = userSettingRepository.findById(ownerId)
                .map(UserSetting::getFeedVisibility)
                .orElse(Visibility.FRIENDS_ONLY);

        if (feedVisibility == Visibility.ONLY_ME) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (feedVisibility == Visibility.FRIENDS_ONLY) {
            boolean isFriend = friendRepository.existsFriendship(userId, ownerId);
            if (!isFriend) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }

        if (!storyPhotoRepository.existsByStoryIdAndType(storyId, type)) {
            throw new CustomException(ErrorCode.STORY_PHOTO_NOT_FOUND);
        }

        Optional<StoryLike> existing = storyLikeRepository
                .findByStoryIdAndUserIdAndType(storyId, userId, type);

        boolean liked;
        if (existing.isPresent()) {
            storyLikeRepository.delete(existing.get());
            liked = false;
        } else {
            storyLikeRepository.save(
                    StoryLike.builder()
                            .storyId(storyId)
                            .userId(userId)
                            .type(type)
                            .build()
            );
            liked = true;

            eventPublisher.publishEvent(new StoryLikedEvent(storyId, userId, ownerId, type));
        }

        return new StoryLikeResponse(storyId, type, liked);
    }

    @Transactional(readOnly = true)
    public List<StoryLikeListResponse> getLikes(Long storyId, AlbumPhotoType type, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORY_NOT_FOUND));

        if (!story.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (!storyPhotoRepository.existsByStoryIdAndType(storyId, type)) {
            throw new CustomException(ErrorCode.STORY_PHOTO_NOT_FOUND);
        }

        List<StoryLike> likes = storyLikeRepository
                .findByStoryIdAndTypeOrderByCreatedAtDesc(storyId, type);
        if (likes.isEmpty()) {
            return List.of();
        }

        List<Long> likeUserIds = likes.stream().map(StoryLike::getUserId).toList();
        Map<Long, User> userMap = userRepository.findAllById(likeUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<StoryLikeListResponse> result = new ArrayList<>();
        for (StoryLike like : likes) {
            User user = userMap.get(like.getUserId());
            if (user == null) continue;

            result.add(new StoryLikeListResponse(
                    user.getId(),
                    user.getHandle(),
                    user.getUsername(),
                    user.getProfileImageUrl(),
                    like.getCreatedAt()
            ));
        }

        return result;
    }
}
