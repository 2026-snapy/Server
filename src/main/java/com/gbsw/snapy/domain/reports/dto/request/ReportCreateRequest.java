package com.gbsw.snapy.domain.reports.dto.request;

import com.gbsw.snapy.domain.reports.entity.ReportReason;
import com.gbsw.snapy.domain.reports.entity.ReportTargetType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull(message = "신고 대상 타입은 필수입니다.")
        ReportTargetType targetType,

        @Positive(message = "신고 대상 ID는 양수여야 합니다.")
        Long targetId,

        @Size(max = 25, message = "신고 대상 유저 핸들은 25자를 초과할 수 없습니다.")
        String userHandle,

        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason
) {
    @AssertTrue(message = "신고 대상 정보가 올바르지 않습니다.")
    public boolean isValidTargetValue() {
        if (targetType == null) {
            return true;
        }

        if (targetType == ReportTargetType.PROFILE) {
            return userHandle != null && !userHandle.isBlank();
        }

        return targetId != null;
    }
}
