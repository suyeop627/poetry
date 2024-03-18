package com.study.poetry.dto.report;

import com.study.poetry.dto.admin.ReportDetailsStatisticsInterface;
import com.study.poetry.entity.PoemSettings;
import com.study.poetry.utils.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
//신고 상세 내역 조회시 응답 dto
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ComprehensiveReportDto {
  private Long reportId;
  private LocalDateTime creationDate;
  private ReportStatus reportStatus;
  private Long poemId;
  private String doneByName;
  private String doneByEmail;

  private List<ReportDetailsDto> reportDetails;

  private PoemSummaryForReportResponseDto poem;
  private PoemSettings poemSettings;
  private ReportDetailsStatisticsInterface reportDetailsStatistics;
  private LocalDateTime memberRestrictionEndDate;
  private String restrictionReason;
}
