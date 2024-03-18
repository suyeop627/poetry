package com.study.poetry.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
//신고대상에 대한 각 회원의 신고 상세 응답 dto(ComprehensiveReportDto 에 포함)
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ReportDetailsDto {
  private Long reportDetailsId;
  private LocalDateTime reportDate;

  private Long memberId;
  private String name;
  private String email;
  private Set<String> reportReasons;
  private String reportComment;
}
