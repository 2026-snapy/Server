package com.gbsw.snapy.infra.fcm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "firebase")
public class FcmProperties {

    private boolean enabled = false;
    private String serviceAccountPath;
}
