package com.gbsw.snapy.domain.auth.controller;

import com.gbsw.snapy.domain.auth.dto.request.GoogleIosLoginRequest;
import com.gbsw.snapy.domain.auth.dto.response.LoginResponse;
import com.gbsw.snapy.domain.auth.dto.response.LoginServiceResult;
import com.gbsw.snapy.domain.auth.service.GoogleOAuthService;
import com.gbsw.snapy.global.common.ApiResponse;
import com.gbsw.snapy.global.oauth.GoogleOAuthProperties;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleOAuthProperties googleOAuthProperties;
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
}
