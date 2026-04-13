package com.gbsw.snapy.domain.settings.dto.response;

import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.entity.Visibility;

public record UserSettingResponse(
        Visibility feedVisibility,
        Visibility albumVisibility
) {
    public static UserSettingResponse from(UserSetting setting) {
        return new UserSettingResponse(setting.getFeedVisibility(), setting.getAlbumVisibility());
    }
}
