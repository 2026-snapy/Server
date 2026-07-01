package com.gbsw.snapy.domain.auth.service;

import com.gbsw.snapy.domain.auth.dto.request.LoginRequest;
import com.gbsw.snapy.domain.auth.entity.OAuthProvider;
import com.gbsw.snapy.domain.auth.repository.RefreshTokenRepository;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import com.gbsw.snapy.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserSettingRepository userSettingRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void localLoginWithEmailRegisteredByOAuthThrowsDedicatedError() {
        LoginRequest request = mock(LoginRequest.class);
        User googleUser = User.builder()
                .id(1L)
                .handle("google")
                .username("name")
                .email("user@example.com")
                .provider(OAuthProvider.GOOGLE)
                .providerId("google-sub")
                .build();

        when(request.getEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_REGISTERED_WITH_DIFFERENT_PROVIDER);
        verifyNoInteractions(passwordEncoder, jwtProvider, refreshTokenRepository);
    }
}
