package com.gbsw.snapy.domain.settings.dto.request;

import com.gbsw.snapy.domain.settings.entity.Visibility;
import jakarta.validation.constraints.NotNull;

public record UpdateAlbumVisibilityRequest(
        @NotNull(message = "visibility는 필수입니다. (PUBLIC, FRIENDS_ONLY, ONLY_ME)")
        Visibility visibility
) {
}
