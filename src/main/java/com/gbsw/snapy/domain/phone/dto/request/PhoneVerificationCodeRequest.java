package com.gbsw.snapy.domain.phone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneVerificationCodeRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$", message = "휴대폰 번호는 11자리 숫자여야 합니다.")
    private String phone;
}
