package com.gbsw.snapy.domain.reports.dto.response;

import com.gbsw.snapy.domain.reports.entity.Report;
import com.gbsw.snapy.domain.reports.entity.ReportReason;
import com.gbsw.snapy.domain.reports.entity.ReportTargetType;

public record ReportCreateResponse(
        Long reportId,
        ReportTargetType targetType,
        Long targetId,
        String userHandle,
        ReportReason reason
) {
    public static ReportCreateResponse from(Report report) {
        return new ReportCreateResponse(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getUserHandle(),
                report.getReason()
        );
    }
}
