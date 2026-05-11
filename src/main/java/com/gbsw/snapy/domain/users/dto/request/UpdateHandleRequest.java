package com.gbsw.snapy.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateHandleRequest {

    @NotBlank(message = "핸들은 공백일 수 없습니다.")
    @Size(max = 25, message = "Handle은 25자를 초과할 수 없습니다.")
    private String handle;
}
