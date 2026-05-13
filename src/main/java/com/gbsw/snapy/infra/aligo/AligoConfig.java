package com.gbsw.snapy.infra.aligo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AligoConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public RestClient aligoRestClient(AligoProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);

        return RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .requestFactory(factory)
                .build();
    }
}
