package com.gbsw.snapy.domain.auth.service;

import com.gbsw.snapy.domain.auth.dto.internal.GoogleUserInfo;
import com.gbsw.snapy.domain.auth.entity.OAuthProvider;
import com.gbsw.snapy.domain.auth.repository.RefreshTokenRepository;
import com.gbsw.snapy.domain.settings.repository.UserSettingRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.global.oauth.GoogleOAuthProperties;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import com.gbsw.snapy.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserSettingRepository userSettingRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private JwtProperties jwtProperties;
    @Mock private GoogleOAuthProperties googleOAuthProperties;
    @Mock private GoogleIdTokenValidator googleIdTokenValidator;

    @InjectMocks
    private GoogleOAuthService googleOAuthService;

    private final GoogleOAuthProperties.Web web = new GoogleOAuthProperties.Web();
    private final GoogleOAuthProperties.Ios ios = new GoogleOAuthProperties.Ios();
    private GoogleUserInfo userInfo;

    @BeforeEach
    void setUp() {
        web.setClientId("web-client-id");
        ios.setClientId("ios-client-id");
        userInfo = new GoogleUserInfo(
                "google-sub", "user@example.com", "name", null, null, true);
    }

    @Test
    void androidLoginVerifiesTokenWithWebClientId() {
        stubExistingUserLogin();
        when(googleOAuthProperties.getWeb()).thenReturn(web);
        when(googleIdTokenValidator.verify("android-id-token", "web-client-id"))
                .thenReturn(userInfo);

        googleOAuthService.processAndroidLogin("android-id-token");

        verify(googleIdTokenValidator).verify("android-id-token", "web-client-id");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void iosLoginStillVerifiesTokenWithIosClientId() {
        stubExistingUserLogin();
        when(googleOAuthProperties.getIos()).thenReturn(ios);
        when(googleIdTokenValidator.verify("ios-id-token", "ios-client-id"))
                .thenReturn(userInfo);

        googleOAuthService.processIosLogin("ios-id-token");

        verify(googleIdTokenValidator).verify("ios-id-token", "ios-client-id");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void loginWithEmailRegisteredByAnotherMethodThrowsDedicatedError() {
        User localUser = User.builder()
                .id(2L)
                .handle("local")
                .username("name")
                .email("user@example.com")
                .provider(OAuthProvider.LOCAL)
                .build();

        when(googleOAuthProperties.getWeb()).thenReturn(web);
        when(googleIdTokenValidator.verify("android-id-token", "web-client-id"))
                .thenReturn(userInfo);
        when(userRepository.findByProviderIdAndProvider("google-sub", OAuthProvider.GOOGLE))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> googleOAuthService.processAndroidLogin("android-id-token"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_REGISTERED_WITH_DIFFERENT_PROVIDER);
    }

    @Test
    void newGoogleUserWithoutNameUsesFallbackUsername() {
        GoogleUserInfo userWithoutName = new GoogleUserInfo(
                "google-sub", "user@example.com", null, null, null, true);

        when(googleOAuthProperties.getWeb()).thenReturn(web);
        when(googleIdTokenValidator.verify("android-id-token", "web-client-id"))
                .thenReturn(userWithoutName);
        when(userRepository.findByProviderIdAndProvider("google-sub", OAuthProvider.GOOGLE))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        }).when(userRepository).save(any(User.class));
        when(jwtProvider.generateAccessToken(1L)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(60_000L);

        googleOAuthService.processAndroidLogin("android-id-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("Google User");
    }

    private void stubExistingUserLogin() {
        User user = User.builder()
                .id(1L)
                .handle("g_google")
                .username("name")
                .email("user@example.com")
                .provider(OAuthProvider.GOOGLE)
                .providerId("google-sub")
                .build();

        when(userRepository.findByProviderIdAndProvider("google-sub", OAuthProvider.GOOGLE))
                .thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(1L)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(60_000L);
    }
}
