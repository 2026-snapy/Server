package com.gbsw.snapy.domain.auth.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserInfo {

    private String sub;
    private String email;
    private String name;
    private String picture;
    private String aud;

    @JsonProperty("email_verified")
    private boolean emailVerified;
}
