package com.gbsw.snapy.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUsernameRequest {

    @NotBlank(message = "이름은 공백일 수 없습니다.")
    @Size(max = 25, message = "이름은 25자를 초과할 수 없습니다.")
    private String username;
}
