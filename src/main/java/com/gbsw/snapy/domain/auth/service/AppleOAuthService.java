package com.gbsw.snapy.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbsw.snapy.domain.auth.dto.internal.ApplePublicKeys;
import com.gbsw.snapy.domain.auth.dto.internal.AppleTokenResponse;
import com.gbsw.snapy.domain.auth.dto.internal.AppleUserInfo;
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
import com.gbsw.snapy.global.oauth.AppleOAuthProperties;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import com.gbsw.snapy.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppleOAuthService {

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_JWKS_URL  = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER    = "https://appleid.apple.com";

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final AppleOAuthProperties appleOAuthProperties;
    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.create();

    // ── iOS ─────────────────────────────────────────────────────────────────────

    @Transactional
    public LoginServiceResult processIosLogin(String identityToken, String fullName) {
        AppleUserInfo userInfo = verifyAppleToken(identityToken, appleOAuthProperties.getBundleId());
        return processOAuthLogin(userInfo, fullName);
    }

    // ── 웹 ──────────────────────────────────────────────────────────────────────

    @Transactional
    public LoginServiceResult processWebLogin(String code, String userJson) {
        String clientSecret = generateClientSecret();
        AppleTokenResponse tokenResponse = exchangeCodeForToken(code, clientSecret);
        AppleUserInfo userInfo = verifyAppleToken(tokenResponse.getIdToken(), appleOAuthProperties.getServiceId());

        String fullName = extractFullName(userJson);
        return processOAuthLogin(userInfo, fullName);
    }

    // ── 공통 로그인 진입점 ─────────────────────────────────────────────────────

    private LoginServiceResult processOAuthLogin(AppleUserInfo userInfo, String fullName) {
        Optional<User> existing = userRepository.findByProviderIdAndProvider(userInfo.getSub(), OAuthProvider.APPLE);

        User user = existing.isPresent()
                ? existing.get()
                : registerOAuthUser(userInfo, fullName);

        if (user.isDeleted()) {
            user.restore();
        }

        return loginOAuthUser(user);
    }

    // ── 회원가입 ────────────────────────────────────────────────────────────────

    private User registerOAuthUser(AppleUserInfo userInfo, String fullName) {
        if (userInfo.getEmail() == null) {
            throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
        }

        userRepository.findByEmail(userInfo.getEmail()).ifPresent(u -> {
            throw new CustomException(ErrorCode.EMAIL_REGISTERED_WITH_DIFFERENT_PROVIDER);
        });

        User user = User.builder()
                .handle(generateUniqueHandle(userInfo.getSub()))
                .username(fullName != null && !fullName.isBlank() ? fullName : "Apple User")
                .email(userInfo.getEmail())
                .provider(OAuthProvider.APPLE)
                .providerId(userInfo.getSub())
                .build();

        userRepository.save(user);
        userSettingRepository.save(UserSetting.builder().userId(user.getId()).build());
        return user;
    }

    // ── 로그인 (토큰 발급) ─────────────────────────────────────────────────────

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

    // ── Apple identity token 검증 ──────────────────────────────────────────────

    private AppleUserInfo verifyAppleToken(String identityToken, String expectedAudience) {
        try {
            String[] parts = identityToken.split("\\.");
            if (parts.length != 3) throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            JsonNode header = objectMapper.readTree(headerJson);
            String kid = header.get("kid").asText();

            ApplePublicKeys publicKeys = fetchApplePublicKeys();
            ApplePublicKeys.Key matchingKey = publicKeys.getKeys().stream()
                    .filter(k -> kid.equals(k.getKid()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.APPLE_LOGIN_FAILED));

            RSAPublicKey publicKey = buildRsaPublicKey(matchingKey);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();

            if (!APPLE_ISSUER.equals(claims.getIssuer()))
                throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
            if (!claims.getAudience().contains(expectedAudience))
                throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);

            return AppleUserInfo.builder()
                    .sub(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
        }
    }

    // ── 웹: authorization code → token 교환 ───────────────────────────────────

    private AppleTokenResponse exchangeCodeForToken(String code, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id",     appleOAuthProperties.getServiceId());
        params.add("client_secret", clientSecret);
        params.add("code",          code);
        params.add("grant_type",    "authorization_code");
        params.add("redirect_uri",  appleOAuthProperties.getRedirectUri());

        try {
            return restClient.post()
                    .uri(APPLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(AppleTokenResponse.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
        }
    }

    // ── 웹: client_secret 생성 (ES256 JWT, 유효기간 5분) ─────────────────────

    private String generateClientSecret() {
        try {
            String stripped = appleOAuthProperties.getPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(stripped);
            PrivateKey privateKey = KeyFactory.getInstance("EC")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

            long now = System.currentTimeMillis() / 1000;

            return Jwts.builder()
                    .header().add("kid", appleOAuthProperties.getKeyId()).and()
                    .issuer(appleOAuthProperties.getTeamId())
                    .subject(appleOAuthProperties.getServiceId())
                    .audience().add(APPLE_ISSUER).and()
                    .issuedAt(new Date(now * 1000))
                    .expiration(new Date((now + 300) * 1000))
                    .signWith(privateKey, Jwts.SIG.ES256)
                    .compact();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
        }
    }

    // ── Apple JWKS 조회 ────────────────────────────────────────────────────────

    private ApplePublicKeys fetchApplePublicKeys() {
        try {
            return restClient.get()
                    .uri(APPLE_JWKS_URL)
                    .retrieve()
                    .body(ApplePublicKeys.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
        }
    }

    private RSAPublicKey buildRsaPublicKey(ApplePublicKeys.Key key) {
        try {
            byte[] n = Base64.getUrlDecoder().decode(key.getN());
            byte[] e = Base64.getUrlDecoder().decode(key.getE());
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, n), new BigInteger(1, e));
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_LOGIN_FAILED);
        }
    }

    // ── 유틸 ────────────────────────────────────────────────────────────────────

    private String extractFullName(String userJson) {
        if (userJson == null) return null;
        try {
            JsonNode nameNode = objectMapper.readTree(userJson).path("name");
            String first = nameNode.path("firstName").asText("").trim();
            String last  = nameNode.path("lastName").asText("").trim();
            String full  = (first + " " + last).trim();
            return full.isEmpty() ? null : full;
        } catch (Exception e) {
            return null;
        }
    }

    // TODO: 동시성 문제 발생시 트랜잭션 적용 필요
    private String generateUniqueHandle(String appleSub) {
        String base = "a_" + appleSub.substring(0, Math.min(8, appleSub.length()));
        if (!userRepository.existsByHandle(base)) return base;
        return "a_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
