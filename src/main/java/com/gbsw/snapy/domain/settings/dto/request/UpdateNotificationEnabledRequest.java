package com.gbsw.snapy.domain.settings.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationEnabledRequest(
        @NotNull(message = "enabled는 필수입니다.")
        Boolean enabled
) {
}
