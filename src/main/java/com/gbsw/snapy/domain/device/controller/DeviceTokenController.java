package com.gbsw.snapy.domain.device.controller;

import com.gbsw.snapy.domain.device.dto.request.DeviceTokenRegisterRequest;
import com.gbsw.snapy.domain.device.service.DeviceTokenService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody DeviceTokenRegisterRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        deviceTokenService.register(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam String token,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        deviceTokenService.delete(principal.getId(), token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
