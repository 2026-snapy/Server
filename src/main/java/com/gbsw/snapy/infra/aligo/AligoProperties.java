package com.gbsw.snapy.infra.aligo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aligo")
@Getter
@Setter
public class AligoProperties {

    private String key;
    private String userId;
    private String sender;
    private String apiUrl = "https://apis.aligo.in/send/";
    private boolean testMode = false;
}
