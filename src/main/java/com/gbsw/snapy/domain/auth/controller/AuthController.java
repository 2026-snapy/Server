package com.gbsw.snapy.domain.auth.controller;

import com.gbsw.snapy.domain.auth.dto.request.LoginRequest;
import com.gbsw.snapy.domain.auth.dto.request.RegisterRequest;
import com.gbsw.snapy.domain.auth.dto.response.LoginResponse;
import com.gbsw.snapy.domain.auth.dto.response.LoginServiceResult;
import com.gbsw.snapy.domain.auth.dto.response.RefreshAccessTokenResponse;
import com.gbsw.snapy.domain.auth.dto.response.RegisterResponse;
import com.gbsw.snapy.domain.auth.service.AuthService;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import com.gbsw.snapy.global.common.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest dto
    ) {
        RegisterResponse rs = authService.register(dto);

        return ResponseEntity.ok(ApiResponse.success(rs));
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LoginServiceResult result = authService.login(dto);
        boolean isApp = "app".equals(request.getHeader("X-Client-Type"));

        if (isApp) {
            return ResponseEntity.ok(ApiResponse.success(new LoginResponse(result.accessToken(), result.refreshToken())));
        }

        Cookie cookie = new Cookie("refreshToken", result.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(result.accessToken(), null)));
    }

    @PostMapping("refresh-accesstoken")
    public ResponseEntity<ApiResponse<RefreshAccessTokenResponse>> refreshAccessToken(
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "X-Refresh-Token", required = false) String appRefreshToken,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken
    ) {
        String refreshToken = "app".equals(clientType) ? appRefreshToken : cookieRefreshToken;
        RefreshAccessTokenResponse rs = authService.refreshAcessToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(rs));
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "X-Refresh-Token", required = false) String appRefreshToken,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse response
    ) {
        String refreshToken = "app".equals(clientType) ? appRefreshToken : cookieRefreshToken;
        authService.logout(refreshToken);

        if (!"app".equals(clientType)) {
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
