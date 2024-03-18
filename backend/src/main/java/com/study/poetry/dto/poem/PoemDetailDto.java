package com.study.poetry.dto.poem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//게시글 상세 조회시 응답 dto
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoemDetailDto {
  private PoemSummaryDto poemSummaryDto;
  private Long id;
  private String titleFontSize;
  private String contentFontSize;
  private String fontFamily;
  private String color;
  private String textAlign;
  private String backgroundImage;
  private Float backgroundOpacity;

}
