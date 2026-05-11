package com.gbsw.snapy.domain.device.dto.request;

import com.gbsw.snapy.domain.device.entity.DevicePlatform;
import com.gbsw.snapy.domain.device.entity.DeviceTokenEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenRegisterRequest(
        @NotBlank String token,
        @NotNull DevicePlatform platform,
        @NotNull DeviceTokenEnvironment environment
) {
}
