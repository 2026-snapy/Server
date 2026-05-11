package com.gbsw.snapy.infra.apns;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@EnableConfigurationProperties(ApnsProperties.class)
public class ApnsConfig {

    @Bean
    @ConditionalOnProperty(prefix = "apns", name = "enabled", havingValue = "true")
    public ApnsClient apnsClient(ApnsProperties properties) throws Exception {
        String apnsServer = properties.isProduction()
                ? ApnsClientBuilder.PRODUCTION_APNS_HOST
                : ApnsClientBuilder.DEVELOPMENT_APNS_HOST;

        return new ApnsClientBuilder()
                .setApnsServer(apnsServer)
                .setClientCredentials(
                        new File(properties.getCertificatePath()),
                        properties.getCertificatePassword()
                )
                .build();
    }
}
