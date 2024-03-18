package com.study.poetry.dto.utils;

import lombok.Data;
//신고 목록 페이지의 pagination 관련 정보 전달 dto
@Data
public class ReportPageSearchDto {
  private Integer page=1;
  private Integer size=10;
  private String title;
  private String content;
  private String name;
  private String email;
  private String writer;
  private String reportStatus;
  private String orderCondition;
  private String direction;
  private String startDate;
  private String endDate;
}
