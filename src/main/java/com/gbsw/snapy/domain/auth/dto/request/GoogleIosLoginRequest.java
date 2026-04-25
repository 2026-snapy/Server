package com.gbsw.snapy.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleIosLoginRequest {

    @NotBlank
    private String idToken;
}
