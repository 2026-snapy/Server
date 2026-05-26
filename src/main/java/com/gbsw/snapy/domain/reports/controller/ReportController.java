package com.gbsw.snapy.domain.reports.controller;

import com.gbsw.snapy.domain.reports.dto.request.ReportCreateRequest;
import com.gbsw.snapy.domain.reports.dto.response.ReportCreateResponse;
import com.gbsw.snapy.domain.reports.service.ReportService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportCreateResponse>> create(
            @Valid @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ReportCreateResponse response = reportService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
