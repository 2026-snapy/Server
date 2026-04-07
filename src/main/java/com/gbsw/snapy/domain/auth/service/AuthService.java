package com.gbsw.snapy.domain.auth.service;

import com.gbsw.snapy.domain.auth.dto.request.LoginRequest;
import com.gbsw.snapy.domain.auth.dto.request.RegisterRequest;
import com.gbsw.snapy.domain.auth.dto.response.LoginServiceResult;
import com.gbsw.snapy.domain.auth.dto.response.RefreshAccessTokenResponse;
import com.gbsw.snapy.domain.auth.dto.response.RegisterResponse;
import com.gbsw.snapy.domain.auth.entity.RefreshToken;
import com.gbsw.snapy.domain.auth.repository.RefreshTokenRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import com.gbsw.snapy.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public RegisterResponse register(RegisterRequest dto) {
        if (userRepository.existsByHandle(dto.getHandle())) {
            throw new CustomException(ErrorCode.DUPLICATE_HANDLE);
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new CustomException(ErrorCode.DUPLICATE_PHONE);
        }

        User user = User.builder()
                .handle(dto.getHandle())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        userRepository.save(user);

        return RegisterResponse.from(user);
    }

    @Transactional
    public LoginServiceResult login(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        RefreshToken refreshTokenDB = RefreshToken.builder()
                .token(hash(refreshToken))
                .expiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
                .user(user)
                .build();

        refreshTokenRepository.save(refreshTokenDB);

        return new LoginServiceResult(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(hash(refreshToken));
        }
    }

    @Transactional
    public RefreshAccessTokenResponse refreshAcessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        jwtProvider.validateToken(refreshToken);

        RefreshToken refreshTokenDB = refreshTokenRepository.findByToken(hash(refreshToken))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshTokenDB.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshTokenDB);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(refreshTokenDB.getUser().getId());

        return new RefreshAccessTokenResponse(newAccessToken);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}