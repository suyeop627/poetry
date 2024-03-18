package com.study.poetry.dto.poem;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

//게시글 작성 시, 설정 저장 요청 dto
@Data
public class PoemSettingsSaveRequestDto {

  @NotNull(message = "titleFontSize must not be null")
  private String titleFontSize;

  @NotNull(message = "contentFontSize must not be null")
  private String contentFontSize;

  @NotNull(message = "fontFamily must not be null")
  private String fontFamily;

  @NotNull(message = "color must not be null")
  private String color;

  @NotNull(message = "textAlign must not be null")
  private String textAlign;

  private String backgroundImage;

  @NotNull(message = "backgroundOpacity must not be null")
  private Float backgroundOpacity;
}
