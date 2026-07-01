package com.gbsw.snapy.domain.auth.service;

import com.gbsw.snapy.domain.auth.dto.internal.GoogleTokenResponse;
import com.gbsw.snapy.domain.auth.dto.internal.GoogleUserInfo;
import com.gbsw.snapy.domain.auth.dto.response.LoginServiceResult;
import com.gbsw.snapy.domain.auth.entity.OAuthProvider;
import com.gbsw.snapy.domain.auth.entity.RefreshToken;
import com.gbsw.snapy.domain.auth.repository.RefreshTokenRepository;
import com.gbsw.snapy.domain.settings.entity.UserSetting;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.global.oauth.GoogleOAuthProperties;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import com.gbsw.snapy.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String GOOGLE_TOKEN_URL    = "https://oauth2.googleapis.com/token";

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final GoogleIdTokenValidator googleIdTokenValidator;

    private final RestClient restClient = RestClient.create();

    // ── 웹 ─────────────────────────────────────────────────────────────────────

    @Transactional
    public LoginServiceResult processWebLogin(String code) {
        GoogleTokenResponse tokenResponse = exchangeCodeForToken(code);
        GoogleUserInfo userInfo = googleIdTokenValidator.verify(
                tokenResponse.getIdToken(), googleOAuthProperties.getWeb().getClientId());
        return processOAuthLogin(userInfo);
    }

    // ── iOS ────────────────────────────────────────────────────────────────────

    /**
     * iOS: Google Sign-In SDK에서 받은 ID token으로 로그인
     * iOS 앱 전용 OAuth 클라이언트 ID를 Google Console에 등록 후 aud 검증 추가 필요
     */
    @Transactional
    public LoginServiceResult processIosLogin(String idToken) {
        GoogleUserInfo userInfo = googleIdTokenValidator.verify(
                idToken, googleOAuthProperties.getIos().getClientId());
        return processOAuthLogin(userInfo);
    }

    // ── Android ──────────────────────────────────────────────────────────────────

    /**
     * Android: Credential Manager에서 받은 ID token으로 로그인.
     * Android의 serverClientId는 서버의 Web OAuth client ID와 같아야 한다.
     */
    @Transactional
    public LoginServiceResult processAndroidLogin(String idToken) {
        GoogleUserInfo userInfo = googleIdTokenValidator.verify(
                idToken, googleOAuthProperties.getWeb().getClientId());
        return processOAuthLogin(userInfo);
    }

    // ── 공통 로그인 진입점 ──────────────────────────────────────────────────────

    /**
     * 유저 존재 여부에 따라 회원가입 or 로그인 분기
     * AuthService.register() → AuthService.login() 흐름과 동일
     */
    private LoginServiceResult processOAuthLogin(GoogleUserInfo userInfo) {
        Optional<User> existing = userRepository.findByProviderIdAndProvider(
                userInfo.getSub(), OAuthProvider.GOOGLE);

        User user = existing.isPresent()
                ? existing.get()                 // 기존 유저 → 로그인
                : registerOAuthUser(userInfo);   // 신규 유저 → 회원가입 후 로그인

        if (user.isDeleted()) {
            user.restore();
        }

        return loginOAuthUser(user);
    }

    // ── 회원가입 (AuthService.register 와 동일한 역할) ─────────────────────────

    private User registerOAuthUser(GoogleUserInfo userInfo) {
        userRepository.findByEmail(userInfo.getEmail()).ifPresent(u -> {
            throw new CustomException(ErrorCode.EMAIL_REGISTERED_WITH_DIFFERENT_PROVIDER);
        });

        String username = userInfo.getName();
        if (username == null || username.isBlank()) {
            username = "Snapy User";
        }

        User user = User.builder()
                .handle(generateUniqueHandle(userInfo.getSub()))
                .username(username)
                .email(userInfo.getEmail())
                .provider(OAuthProvider.GOOGLE)
                .providerId(userInfo.getSub())
                .profileImageUrl(userInfo.getPicture())
                .build();

        userRepository.save(user);
        userSettingRepository.save(UserSetting.builder().userId(user.getId()).build());

        return user;
    }

    // ── 로그인 (AuthService.login 토큰 발급 부분과 동일) ──────────────────────

    private LoginServiceResult loginOAuthUser(User user) {
        String accessToken  = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .token(hash(refreshToken))
                .expiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
                .user(user)
                .build());

        return new LoginServiceResult(accessToken, refreshToken);
    }

    // ── Google API 호출 ────────────────────────────────────────────────────────

    private GoogleTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code",          code);
        params.add("client_id",     googleOAuthProperties.getWeb().getClientId());
        params.add("client_secret", googleOAuthProperties.getWeb().getClientSecret());
        params.add("redirect_uri",  googleOAuthProperties.getWeb().getRedirectUri());
        params.add("grant_type",    "authorization_code");

        try {
            return restClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GOOGLE_LOGIN_FAILED);
        }
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────────

    // TODO: 동시성 문제 발생시 트랜잭션 적용 필요
    private String generateUniqueHandle(String googleSub) {
        String base = "g_" + googleSub.substring(0, Math.min(8, googleSub.length()));
        if (!userRepository.existsByHandle(base)) {
            return base;
        }
        return "g_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
