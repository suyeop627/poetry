package com.study.poetry.dto.utils;

import lombok.Data;

//페이지네이션 관련 정보 전달 dto
@Data
public class PageParameterDto {

  private Integer page=1;

  private Integer size=10;
  //for poem search
  private Integer categoryId=1;

  private String type="";

  private String keyword="";
}
