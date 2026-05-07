package com.gbsw.snapy.domain.auth.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppleUserInfo {
    private String sub;
    private String email;
}
