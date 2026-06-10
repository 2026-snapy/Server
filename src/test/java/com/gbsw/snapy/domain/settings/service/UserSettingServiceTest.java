package com.gbsw.snapy.domain.settings.service;

import com.gbsw.snapy.domain.settings.dto.request.UpdateNotificationEnabledRequest;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingServiceTest {

    @Mock
    private UserSettingRepository userSettingRepository;

    @InjectMocks
    private UserSettingService userSettingService;

    @Test
    void updateNotificationEnabledDisablesPushNotifications() {
        UserSetting setting = UserSetting.builder().userId(1L).build();
        when(userSettingRepository.findById(1L)).thenReturn(Optional.of(setting));

        userSettingService.updateNotificationEnabled(1L, new UpdateNotificationEnabledRequest(false));

        assertThat(setting.isNotificationEnabled()).isFalse();
    }
}
