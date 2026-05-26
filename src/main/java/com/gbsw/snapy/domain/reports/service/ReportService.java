package com.gbsw.snapy.domain.reports.service;

import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.reports.dto.request.ReportCreateRequest;
import com.gbsw.snapy.domain.reports.dto.response.ReportCreateResponse;
import com.gbsw.snapy.domain.reports.entity.Report;
import com.gbsw.snapy.domain.reports.repository.ReportRepository;
import com.gbsw.snapy.domain.stories.repository.StoryRepository;
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
        validateTargetExists(request);

        Report report = reportRepository.save(Report.builder()
                .reporterId(reporterId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .build());

        return ReportCreateResponse.from(report);
    }

    private void validateTargetExists(ReportCreateRequest request) {
        boolean exists = switch (request.targetType()) {
            case FEED -> dailyAlbumRepository.existsById(request.targetId());
            case STORY -> storyRepository.existsById(request.targetId());
            case PROFILE -> userRepository.existsById(request.targetId());
        };

        if (!exists) {
            throw new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }
}
