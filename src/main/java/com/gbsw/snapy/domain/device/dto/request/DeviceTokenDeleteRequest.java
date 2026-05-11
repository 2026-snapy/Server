package com.gbsw.snapy.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenDeleteRequest(
        @NotBlank String token
) {
}
