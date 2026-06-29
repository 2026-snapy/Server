package com.gbsw.snapy.infra.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties(FcmProperties.class)
public class FcmConfig {

    @Bean
    @ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging(FcmProperties properties) throws IOException {
        if (!StringUtils.hasText(properties.getServiceAccountPath())) {
            throw new IllegalStateException("firebase.service-account-path is required when firebase.enabled=true");
        }

        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(properties.getServiceAccountPath())))
                .build())
                : FirebaseApp.getInstance();

        return FirebaseMessaging.getInstance(app);
    }
}
