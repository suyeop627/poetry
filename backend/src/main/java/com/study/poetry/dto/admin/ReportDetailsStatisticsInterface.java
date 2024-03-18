package com.study.poetry.dto.admin;
//관리자 메뉴의 신고상세 페이지의 신고사유별 신고 횟수 정보 응답 dto
public interface ReportDetailsStatisticsInterface {
  Integer getMemberProfileImageReportCount();

  Integer getPoemContentReportCount();

  Integer getPoemIrrelevantCategoryReportCount();

  Integer getPoemBackgroundImageReportCount();

  Integer getMemberNameReportCount();

}
