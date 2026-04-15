package com.gbsw.snapy.domain.comments.dto.request;

import com.gbsw.snapy.domain.comments.entity.AttachmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class CommentUploadRequest {

    @NotNull(message = "댓글 타입은 필수입니다.")
    private AttachmentType type;

    private String emojiValue;

    private MultipartFile file;
}
