package com.gbsw.snapy.domain.streaks.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "streaks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Streak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public Streak(Long userId, LocalDate startDate, LocalDate endDate, int count, boolean active) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = count;
        this.active = active;
    }

    public void extend(LocalDate date) {
        this.endDate = date;
        this.count += 1;
    }

    public void deactivate() {
        this.active = false;
    }
}
