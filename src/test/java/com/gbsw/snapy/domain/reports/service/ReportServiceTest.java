package com.gbsw.snapy.domain.reports.service;

import com.gbsw.snapy.domain.albums.repository.DailyAlbumRepository;
import com.gbsw.snapy.domain.reports.dto.request.ReportCreateRequest;
import com.gbsw.snapy.domain.reports.dto.response.ReportCreateResponse;
import com.gbsw.snapy.domain.reports.entity.Report;
import com.gbsw.snapy.domain.reports.entity.ReportReason;
import com.gbsw.snapy.domain.reports.entity.ReportTargetType;
import com.gbsw.snapy.domain.reports.repository.ReportRepository;
import com.gbsw.snapy.domain.stories.repository.StoryRepository;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private DailyAlbumRepository dailyAlbumRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void createSavesFeedReport() {
        ReportCreateRequest request = new ReportCreateRequest(
                ReportTargetType.FEED,
                10L,
                ReportReason.SPAM_OR_SCAM
        );
        when(dailyAlbumRepository.existsById(10L)).thenReturn(true);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportCreateResponse response = reportService.create(1L, request);

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        Report report = captor.getValue();
        assertThat(report.getReporterId()).isEqualTo(1L);
        assertThat(report.getTargetType()).isEqualTo(ReportTargetType.FEED);
        assertThat(report.getTargetId()).isEqualTo(10L);
        assertThat(report.getReason()).isEqualTo(ReportReason.SPAM_OR_SCAM);
        assertThat(response.targetType()).isEqualTo(ReportTargetType.FEED);
        assertThat(response.targetId()).isEqualTo(10L);
        assertThat(response.reason()).isEqualTo(ReportReason.SPAM_OR_SCAM);
    }

    @Test
    void createSavesStoryReport() {
        ReportCreateRequest request = new ReportCreateRequest(
                ReportTargetType.STORY,
                20L,
                ReportReason.FALSE_INFORMATION
        );
        when(storyRepository.existsById(20L)).thenReturn(true);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportCreateResponse response = reportService.create(1L, request);

        verify(reportRepository).save(any(Report.class));
        assertThat(response.targetType()).isEqualTo(ReportTargetType.STORY);
        assertThat(response.targetId()).isEqualTo(20L);
    }

    @Test
    void createSavesProfileReport() {
        ReportCreateRequest request = new ReportCreateRequest(
                ReportTargetType.PROFILE,
                30L,
                ReportReason.OTHER
        );
        when(userRepository.existsById(30L)).thenReturn(true);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportCreateResponse response = reportService.create(1L, request);

        verify(reportRepository).save(any(Report.class));
        assertThat(response.targetType()).isEqualTo(ReportTargetType.PROFILE);
        assertThat(response.targetId()).isEqualTo(30L);
    }

    @Test
    void createThrowsWhenTargetDoesNotExist() {
        ReportCreateRequest request = new ReportCreateRequest(
                ReportTargetType.FEED,
                999L,
                ReportReason.OTHER
        );
        when(dailyAlbumRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> reportService.create(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REPORT_TARGET_NOT_FOUND);
        verify(reportRepository, never()).save(any(Report.class));
    }
}
