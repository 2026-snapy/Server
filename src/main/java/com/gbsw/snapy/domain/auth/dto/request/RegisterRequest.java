package com.gbsw.snapy.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Size(max = 25)
    private String handle;

    @NotBlank
    @Size(max = 25)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{11}$", message = "숫자 11자리여야 합니다")
    private String phone;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
}