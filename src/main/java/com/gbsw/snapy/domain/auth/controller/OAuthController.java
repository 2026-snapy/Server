package com.gbsw.snapy.domain.auth.controller;

import com.gbsw.snapy.domain.auth.dto.request.AppleIosLoginRequest;
import com.gbsw.snapy.domain.auth.dto.request.GoogleAndroidLoginRequest;
import com.gbsw.snapy.domain.auth.dto.request.GoogleIosLoginRequest;
import com.gbsw.snapy.domain.auth.dto.response.LoginResponse;
import com.gbsw.snapy.domain.auth.dto.response.LoginServiceResult;
import com.gbsw.snapy.domain.auth.service.AppleOAuthService;
import com.gbsw.snapy.domain.auth.service.GoogleOAuthService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.oauth.AppleOAuthProperties;
import com.gbsw.snapy.global.oauth.GoogleOAuthProperties;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final AppleOAuthService appleOAuthService;
    private final AppleOAuthProperties appleOAuthProperties;
    private final JwtProperties jwtProperties;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/auth/google/login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleOAuthProperties.getWeb().getClientId()
                + "&redirect_uri=" + URLEncoder.encode(googleOAuthProperties.getWeb().getRedirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);

        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/auth/google/callback")
    public void handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            HttpServletResponse response
    ) throws IOException {
        if (error != null || code == null) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=cancelled");
            return;
        }

        try {
            LoginServiceResult result = googleOAuthService.processWebLogin(code);

            Cookie cookie = new Cookie("refreshToken", result.refreshToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
            response.addCookie(cookie);

            String redirectUrl = frontendUrl + "/auth/callback?token="
                    + URLEncoder.encode(result.accessToken(), StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Google OAuth callback failed", e);
            response.sendRedirect(frontendUrl + "/auth/error?reason=server_error");
        }
    }

    @PostMapping("/api/auth/google/ios")
    public ResponseEntity<ApiResponse<LoginResponse>> handleIosLogin(
            @Valid @RequestBody GoogleIosLoginRequest request
    ) {
        LoginServiceResult result = googleOAuthService.processIosLogin(request.getIdToken());
        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(result.accessToken(), result.refreshToken())
        ));
    }

    // ── Android ──────────────────────────────────────────────────────────────────

    @PostMapping("/api/auth/google/android")
    public ResponseEntity<ApiResponse<LoginResponse>> handleAndroidLogin(
            @Valid @RequestBody GoogleAndroidLoginRequest request
    ) {
        LoginServiceResult result = googleOAuthService.processAndroidLogin(request.getIdToken());
        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(result.accessToken(), result.refreshToken())
        ));
    }

    // ── Apple ────────────────────────────────────────────────────────────────────

    @GetMapping("/auth/apple/login")
    public void redirectToApple(HttpServletResponse response) throws java.io.IOException {
        String appleAuthUrl = "https://appleid.apple.com/auth/authorize"
                + "?client_id=" + appleOAuthProperties.getServiceId()
                + "&redirect_uri=" + java.net.URLEncoder.encode(appleOAuthProperties.getRedirectUri(), java.nio.charset.StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + java.net.URLEncoder.encode("name email", java.nio.charset.StandardCharsets.UTF_8)
                + "&response_mode=form_post";

        response.sendRedirect(appleAuthUrl);
    }

    @PostMapping("/auth/apple/callback")
    public void handleAppleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String user,
            HttpServletResponse response
    ) throws java.io.IOException {
        if (error != null || code == null) {
            response.sendRedirect(frontendUrl + "/auth/error?reason=cancelled");
            return;
        }

        try {
            LoginServiceResult result = appleOAuthService.processWebLogin(code, user);

            Cookie cookie = new Cookie("refreshToken", result.refreshToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
            response.addCookie(cookie);

            String redirectUrl = frontendUrl + "/auth/callback?token="
                    + java.net.URLEncoder.encode(result.accessToken(), java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Apple OAuth callback failed", e);
            response.sendRedirect(frontendUrl + "/auth/error?reason=server_error");
        }
    }

    @PostMapping("/api/auth/apple/ios")
    public ResponseEntity<ApiResponse<LoginResponse>> handleAppleIosLogin(
            @Valid @RequestBody AppleIosLoginRequest request
    ) {
        LoginServiceResult result = appleOAuthService.processIosLogin(
                request.getIdentityToken(), request.getFullName());
        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(result.accessToken(), result.refreshToken())
        ));
    }
}
