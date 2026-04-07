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
            HttpServletResponse response
    ) {
        LoginServiceResult result = authService.login(dto);

        Cookie cookie = new Cookie("refreshToken", result.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success(new LoginResponse(result.accessToken())));
    }

    @PostMapping("refresh-accesstoken")
    public ResponseEntity<ApiResponse<RefreshAccessTokenResponse>> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        RefreshAccessTokenResponse rs = authService.refreshAcessToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(rs));
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);

        // 클라이언트 RefreshToken 쿠키 만료시키기
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
