package com.gbsw.snapy.infra.aligo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AligoConfig {

    @Bean
    public RestClient aligoRestClient(AligoProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .build();
    }
}
