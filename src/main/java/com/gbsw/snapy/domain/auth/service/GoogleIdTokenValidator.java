package com.gbsw.snapy.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.gbsw.snapy.domain.auth.dto.internal.GoogleUserInfo;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GoogleIdTokenValidator {

    private final HttpTransport transport;
    private final Map<String, GoogleIdTokenVerifier> verifiers = new ConcurrentHashMap<>();

    public GoogleIdTokenValidator() {
        try {
            this.transport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to initialize Google ID token verifier", e);
        }
    }

    public GoogleUserInfo verify(String idToken, String expectedAudience) {
        if (idToken == null || idToken.isBlank()
                || expectedAudience == null || expectedAudience.isBlank()) {
            throw new CustomException(ErrorCode.GOOGLE_LOGIN_FAILED);
        }

        try {
            GoogleIdToken verifiedToken = getVerifier(expectedAudience).verify(idToken);
            if (verifiedToken == null) {
                throw new CustomException(ErrorCode.GOOGLE_LOGIN_FAILED);
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();
            if (payload.getSubject() == null || payload.getSubject().isBlank()
                    || payload.getEmail() == null || payload.getEmail().isBlank()
                    || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new CustomException(ErrorCode.GOOGLE_LOGIN_FAILED);
            }

            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture"),
                    expectedAudience,
                    true
            );
        } catch (CustomException e) {
            throw e;
        } catch (GeneralSecurityException | IOException | RuntimeException e) {
            throw new CustomException(ErrorCode.GOOGLE_LOGIN_FAILED);
        }
    }

    private GoogleIdTokenVerifier getVerifier(String audience) {
        return verifiers.computeIfAbsent(audience, value ->
                new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(value))
                        .build()
        );
    }
}
