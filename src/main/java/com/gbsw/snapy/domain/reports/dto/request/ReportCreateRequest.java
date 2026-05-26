package com.gbsw.snapy.domain.reports.dto.request;

import com.gbsw.snapy.domain.reports.entity.ReportReason;
import com.gbsw.snapy.domain.reports.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @NotNull(message = "신고 대상 타입은 필수입니다.")
        ReportTargetType targetType,

        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long targetId,

        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason
) {
}
