package com.study.poetry.dto.report;

import java.time.LocalDateTime;
//신고 요약 정보 응답 dto(신고 목록 조회시 사용)
public interface ReportSummaryInterface {
  Long getReportId();

  LocalDateTime getCreationDate();

  String getReportStatus();

  String getDoneBy();

  Long getReportCount();

  String getTitle();
  String getWriter();
}
