package com.study.poetry.utils.enums;

import lombok.Getter;

//이미지 사용처 종류
public enum ImageType {
  PROFILE("member%d_profile_image_%s%s"), POEM_BACKGROUND("poem%d_background_image_%s%s");
  @Getter
  private final String imageNameFormat;

  ImageType(String imageNameFormat) {
    this.imageNameFormat = imageNameFormat;
  }

}
