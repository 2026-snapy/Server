package com.gbsw.snapy.domain.albums.service;

import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumListResponse;
import com.gbsw.snapy.domain.albums.dto.response.AlbumTodayResponse;
import com.gbsw.snapy.domain.albums.entity.AlbumPhoto;
import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import com.gbsw.snapy.domain.albums.entity.AlbumStatus;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.AlbumPhotoRepository;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.photos.entity.Photo;
import com.gbsw.snapy.domain.photos.entity.PhotoType;
import com.gbsw.snapy.domain.photos.repository.PhotoRepository;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.entity.Visibility;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlbumQueryService {

    private final DailyAlbumRepository dailyAlbumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final PhotoRepository photoRepository;
    private final UserSettingRepository userSettingRepository;
    private final FriendRepository friendRepository;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public AlbumTodayResponse getTodayAlbum(Long userId) {
        LocalDate today = LocalDate.now(KST_ZONE);
        DailyAlbum album = dailyAlbumRepository.findByUserIdAndAlbumDate(userId, today)
                .orElse(null);
        if (album == null) {
            return null;
        }

        List<PhotoSetView> sets = loadPhotoSets(album.getId());
        List<AlbumTodayResponse.AlbumPhotoSet> mapped = sets.stream()
                .map(s -> new AlbumTodayResponse.AlbumPhotoSet(s.type(), s.frontImageUrl(), s.backImageUrl(), s.createdAt()))
                .toList();
        return AlbumTodayResponse.of(album, mapped);
    }

    @Transactional(readOnly = true)
    public AlbumDetailResponse getAlbumDetail(Long albumId, Long userId) {
        DailyAlbum album = dailyAlbumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (!album.getUserId().equals(userId)) {
            if (album.getStatus() != AlbumStatus.PUBLISHED) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            YearMonth albumMonth = YearMonth.from(album.getAlbumDate());
            YearMonth currentMonth = YearMonth.now(KST_ZONE);
            boolean isCurrentMonth = albumMonth.equals(currentMonth);

            UserSetting setting = userSettingRepository.findById(album.getUserId()).orElse(null);

            if (isCurrentMonth) {
                Visibility v = (setting != null) ? setting.getFeedVisibility() : Visibility.FRIENDS_ONLY;
                if (v == Visibility.FRIENDS_ONLY && !friendRepository.existsFriendship(userId, album.getUserId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
            } else {
                Visibility v = (setting != null) ? setting.getPastAlbumVisibility() : Visibility.FRIENDS_ONLY;
                if (v == Visibility.ONLY_ME) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
                if (v == Visibility.FRIENDS_ONLY && !friendRepository.existsFriendship(userId, album.getUserId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
            }
        }

        List<PhotoSetView> sets = loadPhotoSets(album.getId());
        List<AlbumDetailResponse.AlbumPhotoSet> mapped = sets.stream()
                .map(s -> new AlbumDetailResponse.AlbumPhotoSet(s.type(), s.frontImageUrl(), s.backImageUrl(), s.createdAt()))
                .toList();
        return AlbumDetailResponse.of(album, mapped);
    }

    @Transactional(readOnly = true)
    public List<AlbumListResponse> getAlbumsByMonth(Long targetUserId, int month, Long requesterId) {
        if (month < 1 || month > 12) {
            throw new CustomException(ErrorCode.INVALID_MONTH);
        }

        boolean isOwner = targetUserId.equals(requesterId);
        YearMonth currentMonth = YearMonth.now(KST_ZONE);
        int year = currentMonth.getYear();
        boolean isCurrentMonth = YearMonth.of(year, month).equals(currentMonth);

        if (!isOwner) {
            UserSetting setting = userSettingRepository.findById(targetUserId).orElse(null);

            if (isCurrentMonth) {
                Visibility feedVisibility = (setting != null) ? setting.getFeedVisibility() : Visibility.FRIENDS_ONLY;
                if (feedVisibility == Visibility.FRIENDS_ONLY) {
                    if (!friendRepository.existsFriendship(requesterId, targetUserId)) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                    }
                }
            } else {
                Visibility albumVisibility = (setting != null) ? setting.getPastAlbumVisibility() : Visibility.FRIENDS_ONLY;
                if (albumVisibility == Visibility.ONLY_ME) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
                if (albumVisibility == Visibility.FRIENDS_ONLY) {
                    if (!friendRepository.existsFriendship(requesterId, targetUserId)) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                    }
                }
            }
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DailyAlbum> albums = dailyAlbumRepository
                .findByUserIdAndAlbumDateBetweenOrderByAlbumDateDesc(targetUserId, start, end);
        if (!isOwner) {
            albums = albums.stream()
                    .filter(a -> a.getStatus() == AlbumStatus.PUBLISHED)
                    .toList();
        }
        return buildAlbumListResponses(albums);
    }

    @Transactional(readOnly = true)
    public List<AlbumListResponse> getCalendarThumbnails(Long userId) {
        LocalDate today = LocalDate.now(KST_ZONE);
        LocalDate fiveMonthsAgo = today.minusMonths(5).withDayOfMonth(1);

        List<DailyAlbum> albums = dailyAlbumRepository
                .findByUserIdAndAlbumDateBetweenOrderByAlbumDateDesc(userId, fiveMonthsAgo, today);
        return buildAlbumListResponses(albums);
    }

    private List<AlbumListResponse> buildAlbumListResponses(List<DailyAlbum> albums) {
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

    private List<PhotoSetView> loadPhotoSets(Long albumId) {
        List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByAlbumIdOrderByTypeAsc(albumId);
        if (albumPhotos.isEmpty()) {
            return List.of();
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
        Map<AlbumPhotoType, LocalDateTime> createdAts = new EnumMap<>(AlbumPhotoType.class);
        for (AlbumPhoto ap : albumPhotos) {
            Photo photo = photoById.get(ap.getPhotoId());
            if (ap.getSide() == PhotoType.FRONT) {
                frontUrls.put(ap.getType(), photo.getImageUrl());
                createdAts.put(ap.getType(), photo.getCreatedAt());
            } else {
                backUrls.put(ap.getType(), photo.getImageUrl());
            }
        }

        List<PhotoSetView> sets = new ArrayList<>();
        for (AlbumPhotoType type : AlbumPhotoType.values()) {
            if (frontUrls.containsKey(type) || backUrls.containsKey(type)) {
                sets.add(new PhotoSetView(type, frontUrls.get(type), backUrls.get(type),
                        createdAts.get(type)));
            }
        }
        return sets;
    }

    private record PhotoSetView(AlbumPhotoType type, String frontImageUrl, String backImageUrl,
                                LocalDateTime createdAt) {
    }
}
