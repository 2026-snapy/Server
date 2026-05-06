package com.gbsw.snapy.global.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.oauth")
@Getter
@Setter
public class GoogleOAuthProperties {

    private Web web = new Web();
    private Ios ios = new Ios();

    @Getter
    @Setter
    public static class Web {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    @Getter
    @Setter
    public static class Ios {
        private String clientId;
    }
}
