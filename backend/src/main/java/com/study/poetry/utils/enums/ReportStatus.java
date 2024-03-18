package com.study.poetry.utils.enums;

import lombok.Getter;

//신고 상태
public enum ReportStatus {
  REPORTED("신고 접수"),
  UNDER_REVIEW("검토중"),
  DONE("처리완료");
  @Getter
  private final String description;
  ReportStatus(String description) {
    this.description = description;
  }
  }
