package com.gbsw.snapy.infra.apns;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "apns")
public class ApnsProperties {

    private boolean enabled = false;
    private boolean production = false;
    private String bundleId;
    private String certificatePath;
    private String certificatePassword = "";
}
