package com.gbsw.snapy.domain.feed.service;

import com.gbsw.snapy.domain.albums.dto.response.AlbumDetailResponse;
import com.gbsw.snapy.domain.albums.entity.DailyAlbum;
import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.feed.dto.request.FeedRecommendRequest;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.entity.Visibility;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.global.common.CursorResponse;
import com.snapy.proto.feed.RecommendRequest;
import com.snapy.proto.feed.RecommendResponse;
import com.snapy.proto.feed.RecommendServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final DailyAlbumRepository dailyAlbumRepository;
    private final UserSettingRepository userSettingRepository;
    private final FriendRepository friendRepository;

    private final RecommendServiceGrpc.RecommendServiceBlockingStub recommendServiceStub;

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    // 리펙토링 예정
    public CursorResponse<AlbumDetailResponse> recommend(Long userId, FeedRecommendRequest dto) {
        RecommendRequest request = dto.cursor() == null ?
                RecommendRequest.newBuilder().setUserId(userId).setSize(dto.size()).build()
                : RecommendRequest.newBuilder().setUserId(userId).setSize(dto.size()).setCursor(dto.cursor()).build();

        RecommendResponse response = recommendServiceStub.recommendFeed(request);
        // TODO: findByIdIn() 추가
        // List<DailyAlbum> albums = dailyAlbumRepository.findByIdIn(response.getAlbumIdsList());
        List<DailyAlbum> albums = dailyAlbumRepository.findById(1L).stream().toList();

        List<AlbumDetailResponse> albumDetails = albums.stream()
                .filter((a) -> {
                    if (a.getUserId().equals(userId)) {
                        return true;
                    }

                    YearMonth albumMonth = YearMonth.from(a.getAlbumDate());
                    YearMonth currentMonth = YearMonth.now(KST_ZONE);
                    boolean isCurrentMonth = albumMonth.equals(currentMonth);

                    UserSetting setting = userSettingRepository.findById(a.getUserId()).orElse(null);

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
                    // TODO detail 생성 로직 추가
                    return AlbumDetailResponse.of(a, null);
                }).toList();

        Map<Long, AlbumDetailResponse> detailMap = albumDetails.stream()
                .collect(Collectors.toMap(AlbumDetailResponse::albumId, Function.identity()));

        List<AlbumDetailResponse> orderedDetails = response.getAlbumIdsList().stream()
                .map(detailMap::get)
                .filter(Objects::nonNull)
                .toList();
        return CursorResponse.of(orderedDetails, response.getNextCursor(), response.getHasNext());
    }
}
