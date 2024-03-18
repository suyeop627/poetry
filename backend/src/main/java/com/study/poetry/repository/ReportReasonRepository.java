package com.study.poetry.repository;

import com.study.poetry.entity.ReportReason;
import com.study.poetry.utils.enums.ReportReasonType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
  Optional<ReportReason> findByReportReason(ReportReasonType reportReasonType);
}
