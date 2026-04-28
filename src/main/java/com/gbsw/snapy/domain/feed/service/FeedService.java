package com.gbsw.snapy.domain.feed.service;

import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.albums.service.AlbumQueryService;
import com.gbsw.snapy.domain.feed.dto.request.FeedRecommendRequest;
import com.gbsw.snapy.domain.feed.dto.response.FeedItemResponse;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.entity.Visibility;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.common.CursorResponse;
import com.snapy.proto.feed.RecommendRequest;
import com.snapy.proto.feed.RecommendResponse;
import com.snapy.proto.feed.RecommendServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final DailyAlbumRepository dailyAlbumRepository;
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final FriendRepository friendRepository;

    private final AlbumQueryService albumQueryService;

    private final RecommendServiceGrpc.RecommendServiceBlockingStub recommendServiceStub;

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    // 리펙토링 예정
    public CursorResponse<FeedItemResponse> recommend(Long userId, FeedRecommendRequest dto) {
        RecommendRequest request = dto.cursor() == null ?
                RecommendRequest.newBuilder().setUserId(userId).setSize(dto.size()).build()
                : RecommendRequest.newBuilder().setUserId(userId).setSize(dto.size()).setCursor(dto.cursor()).build();

        RecommendResponse response = recommendServiceStub.recommendFeed(request);
        List<DailyAlbum> albums = dailyAlbumRepository.findByIdIn(response.getAlbumIdsList());

        Map<Long, DailyAlbum> albumMap = albums.stream()
                .collect(Collectors.toMap(DailyAlbum::getId, Function.identity()));
        List<DailyAlbum> orderedAlbums = response.getAlbumIdsList().stream()
                .map(albumMap::get)
                .filter(Objects::nonNull)
                .toList();

        Set<Long> ownerIds = orderedAlbums.stream()
                .map(DailyAlbum::getUserId)
                .collect(Collectors.toSet());
        Set<Long> otherOwnerIds = ownerIds.stream()
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toSet());
        Map<Long, UserSetting> settingByOwnerId = userSettingRepository.findAllById(otherOwnerIds).stream()
                .collect(Collectors.toMap(UserSetting::getUserId, Function.identity()));
        Map<Long, User> userById = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<FeedItemResponse> albumDetails = orderedAlbums.stream()
                .filter((a) -> {
                    if (a.getUserId().equals(userId)) {
                        return true;
                    }

                    YearMonth albumMonth = YearMonth.from(a.getAlbumDate());
                    YearMonth currentMonth = YearMonth.now(KST_ZONE);
                    boolean isCurrentMonth = albumMonth.equals(currentMonth);

                    UserSetting setting = settingByOwnerId.get(a.getUserId());

                    Visibility v;
                    if (isCurrentMonth) {
                        v = (setting != null) ? setting.getFeedVisibility() : Visibility.FRIENDS_ONLY;
                    } else {
                        v = (setting != null) ? setting.getPastAlbumVisibility() : Visibility.FRIENDS_ONLY;
                        if (v == Visibility.ONLY_ME) {
                            return false;
                        }
                    }
                    if (v == Visibility.FRIENDS_ONLY && !friendRepository.existsFriendship(userId, a.getUserId())) {
                        return false;
                    }

                    return true;
                })
                .map((a) -> {
                    LocalDateTime snapshotBoundary = a.getUserId().equals(userId) ? null : a.getPublishedAt();
                    List<AlbumQueryService.PhotoSetView> sets = albumQueryService.loadPhotoSets(a.getId(), snapshotBoundary);
                    List<AlbumDetailResponse.AlbumPhotoSet> mapped = sets.stream()
                            .map(s -> new AlbumDetailResponse.AlbumPhotoSet(s.type(), s.frontImageUrl(), s.backImageUrl(), s.createdAt()))
                            .toList();
                    return FeedItemResponse.of(a, mapped, userById.get(a.getUserId()));
                }).toList();
        return CursorResponse.of(albumDetails, response.getNextCursor(), response.getHasNext());
    }
}
