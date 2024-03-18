package com.study.poetry.utils.enums;

import lombok.Getter;

//제재 종류
public enum RestrictionType {
  //게시글 관련
  CATEGORY_CHANGE("카테고리 변경"),
  BACKGROUND_IMAGE_DELETION("배경 이미지 삭제."),

  POEM_DELETION("게시글 삭제"),

  //회원관련
  NAME_CHANGE("회원 이름 변경"),
  PROFILE_IMAGE_DELETION("프로필 사진 삭제"),
  MEMBER_RESTRICTION("POE-TRY 사용 제한");

  @Getter
  private final String message;
  RestrictionType(String message) {
    this.message = message;
  }
}
