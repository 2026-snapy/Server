package com.gbsw.snapy.domain.settings.service;

import com.gbsw.snapy.domain.settings.dto.request.UpdateAlbumVisibilityRequest;
import com.gbsw.snapy.domain.settings.dto.request.UpdateFeedVisibilityRequest;
import com.gbsw.snapy.domain.settings.dto.response.UserSettingResponse;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.entity.Visibility;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;

    @Transactional
    public UserSettingResponse getSettings(Long userId) {
        UserSetting setting = getOrCreate(userId);
        return UserSettingResponse.from(setting);
    }

    @Transactional
    public void updateFeedVisibility(Long userId, UpdateFeedVisibilityRequest request) {
        if (request.visibility() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "visibility는 필수입니다. (PUBLIC, FRIENDS_ONLY)");
        }
        if (request.visibility() == Visibility.ONLY_ME) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "피드는 PUBLIC, FRIENDS_ONLY만 설정할 수 있습니다.");
        }

        UserSetting setting = getOrCreate(userId);
        setting.setFeedVisibility(request.visibility());
    }

    @Transactional
    public void updateAlbumVisibility(Long userId, UpdateAlbumVisibilityRequest request) {
        if (request.visibility() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "visibility는 필수입니다. (PUBLIC, FRIENDS_ONLY, ONLY_ME)");
        }
        UserSetting setting = getOrCreate(userId);
        setting.setAlbumVisibility(request.visibility());
    }

    private UserSetting getOrCreate(Long userId) {
        return userSettingRepository.findById(userId)
                .orElseGet(() -> userSettingRepository.save(UserSetting.builder().userId(userId).build()));
    }
}
