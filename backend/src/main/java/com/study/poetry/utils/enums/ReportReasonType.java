package com.study.poetry.utils.enums;

import lombok.Getter;

//신고 사유 종류
public enum ReportReasonType {
  POEM_CONTENT("불건전한 내용 포함"),
  POEM_BACKGROUND_IMAGE("불건전한 배경 이미지 사용"),
  POEM_IRRELEVANT_CATEGORY("카테고리와 관련 없는 내용"),
  MEMBER_PROFILE_IMAGE("불건전한 회원 프로필 이미지"),
  MEMBER_NAME("불건전한 회원 이름");
  @Getter
  private final String description;

  ReportReasonType(String description) {
    this.description = description;
  }
}
