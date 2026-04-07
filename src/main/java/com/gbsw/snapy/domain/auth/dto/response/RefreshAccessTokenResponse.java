package com.gbsw.snapy.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshAccessTokenResponse {
    private String accessToken;
}
