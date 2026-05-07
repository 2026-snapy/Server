package com.gbsw.snapy.global.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "apple.oauth")
@Getter
@Setter
public class AppleOAuthProperties {
    private String teamId;
    private String keyId;
    private String privateKey;
    private String bundleId;
    private String serviceId;
    private String redirectUri;
}
