package com.gbsw.snapy.domain.auth.controller;

import com.gbsw.snapy.domain.auth.dto.response.LoginServiceResult;
import com.gbsw.snapy.domain.auth.service.AppleOAuthService;
import com.gbsw.snapy.domain.auth.service.GoogleOAuthService;
import com.gbsw.snapy.global.exception.GlobalExceptionHandler;
import com.gbsw.snapy.global.oauth.AppleOAuthProperties;
import com.gbsw.snapy.global.oauth.GoogleOAuthProperties;
import com.gbsw.snapy.global.security.jwt.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OAuthControllerTest {

    @Mock private GoogleOAuthService googleOAuthService;
    @Mock private GoogleOAuthProperties googleOAuthProperties;
    @Mock private AppleOAuthService appleOAuthService;
    @Mock private AppleOAuthProperties appleOAuthProperties;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private OAuthController oauthController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(oauthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void androidLoginReturnsServerTokens() throws Exception {
        when(googleOAuthService.processAndroidLogin("google-id-token"))
                .thenReturn(new LoginServiceResult("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/google/android")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"google-id-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(googleOAuthService).processAndroidLogin("google-id-token");
    }

    @Test
    void androidLoginRejectsBlankIdToken() throws Exception {
        mockMvc.perform(post("/api/auth/google/android")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

}
