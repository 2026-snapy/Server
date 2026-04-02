package com.gbsw.snapy.domain.albums.dto.request;

import com.gbsw.snapy.domain.albums.entity.AlbumPhotoType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AlbumUploadRequest {

    @NotNull(message = "전면 사진은 필수입니다.")
    private MultipartFile frontImage;

    @NotNull(message = "후면 사진은 필수입니다.")
    private MultipartFile backImage;

    @NotNull(message = "앨범 사진 타입은 필수입니다.")
    private AlbumPhotoType type;
}
