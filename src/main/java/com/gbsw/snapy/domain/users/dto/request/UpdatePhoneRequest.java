package com.gbsw.snapy.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePhoneRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$", message = "휴대폰 번호는 11자리 숫자여야 합니다.")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
    private String code;
}
