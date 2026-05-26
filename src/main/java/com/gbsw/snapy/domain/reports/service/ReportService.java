package com.gbsw.snapy.domain.reports.service;

import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.reports.dto.request.ReportCreateRequest;
import com.gbsw.snapy.domain.reports.dto.response.ReportCreateResponse;
import com.gbsw.snapy.domain.reports.entity.Report;
import com.gbsw.snapy.domain.reports.entity.ReportTargetType;
import com.gbsw.snapy.domain.reports.repository.ReportRepository;
import com.gbsw.snapy.domain.stories.repository.StoryRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final DailyAlbumRepository dailyAlbumRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportCreateResponse create(Long reporterId, ReportCreateRequest request) {
        User profileTarget = validateTargetExists(request);

        Report report = reportRepository.save(Report.builder()
                .reporterId(reporterId)
                .targetType(request.targetType())
                .targetId(resolveTargetId(request, profileTarget))
                .userHandle(resolveUserHandle(request))
                .reason(request.reason())
                .build());

        return ReportCreateResponse.from(report);
    }

    private User validateTargetExists(ReportCreateRequest request) {
        return switch (request.targetType()) {
            case FEED -> {
                if (!dailyAlbumRepository.existsById(request.targetId())) {
                    throw new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND);
                }
                yield null;
            }
            case STORY -> {
                if (!storyRepository.existsById(request.targetId())) {
                    throw new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND);
                }
                yield null;
            }
            case PROFILE -> userRepository.findByHandleAndDeletedAtIsNull(normalizeTargetHandle(request))
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND));
        };
    }

    private String normalizeTargetHandle(ReportCreateRequest request) {
        return request.userHandle() == null ? null : request.userHandle().trim();
    }

    private Long resolveTargetId(ReportCreateRequest request, User profileTarget) {
        return request.targetType() == ReportTargetType.PROFILE
                ? profileTarget.getId()
                : request.targetId();
    }

    private String resolveUserHandle(ReportCreateRequest request) {
        return request.targetType() == ReportTargetType.PROFILE
                ? normalizeTargetHandle(request)
                : null;
    }
}
