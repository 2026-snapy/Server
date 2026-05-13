package com.gbsw.snapy.domain.phone.controller;

import com.gbsw.snapy.domain.phone.dto.request.PhoneVerificationCodeRequest;
import com.gbsw.snapy.domain.phone.service.PhoneVerificationService;
import com.gbsw.snapy.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/phone")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/verification-code")
    public ResponseEntity<ApiResponse<Void>> requestVerificationCode(
            @Valid @RequestBody PhoneVerificationCodeRequest request
    ) {
        phoneVerificationService.issueCode(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
