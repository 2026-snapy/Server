package com.gbsw.snapy.domain.reports.repository;

import com.gbsw.snapy.domain.reports.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
