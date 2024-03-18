package com.study.poetry.dto.poem;

import com.study.poetry.entity.Member;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

//게시글 작성시 본문 저장 요청 dto
@Data
public class PoemSaveRequestDto {

  @NotNull(message = "categoryId must not be null")
  private Integer categoryId;

  @Size(max = 50, message = "title size must be under 50")
  @NotNull(message = "title must not be null")
  private String title;

  @Size(max = 500, message = "content size must be under 500")
  @NotNull(message = "content must not be null")
  private String content;

  @Size(max = 50, message = "description size must be under 50")
  @NotNull(message = "description must not be null")
  private String description;

  private Member member;
}
