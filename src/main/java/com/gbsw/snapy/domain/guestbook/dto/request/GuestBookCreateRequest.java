package com.gbsw.snapy.domain.guestbook.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class GuestBookCreateRequest {

    @NotNull(message = "사진은 필수입니다.")
    private MultipartFile image;
}
