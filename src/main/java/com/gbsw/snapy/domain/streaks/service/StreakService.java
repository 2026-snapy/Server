package com.gbsw.snapy.domain.streaks.service;

import com.gbsw.snapy.domain.streaks.entity.Streak;
import com.gbsw.snapy.domain.streaks.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreakService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final StreakRepository streakRepository;

    @Transactional
    public void recordPublish(Long userId) {
        LocalDate today = LocalDate.now(KST_ZONE);
        Optional<Streak> activeOpt = streakRepository.findByUserIdAndActiveTrue(userId);

        if (activeOpt.isPresent()) {
            Streak active = activeOpt.get();
            LocalDate endDate = active.getEndDate();

            if (endDate.equals(today)) {
                return;
            }
            if (endDate.equals(today.minusDays(1))) {
                active.extend(today);
                return;
            }
            active.deactivate();
        }

        streakRepository.save(
                Streak.builder()
                        .userId(userId)
                        .startDate(today)
                        .endDate(today)
                        .count(1)
                        .active(true)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public StreakSummary getSummary(Long userId) {
        LocalDate today = LocalDate.now(KST_ZONE);
        int current = streakRepository.findByUserIdAndActiveTrue(userId)
                .filter(s -> {
                    LocalDate endDate = s.getEndDate();
                    return endDate.equals(today) || endDate.equals(today.minusDays(1));
                })
                .map(Streak::getCount)
                .orElse(0);
        int max = streakRepository.findMaxCountByUserId(userId);
        return new StreakSummary(current, max);
    }

    public record StreakSummary(int current, int max) {
    }
}
