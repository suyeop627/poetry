package com.study.poetry.dto.poem;

import com.study.poetry.dto.member.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
//게시글 조회시, 요약 정보 응답 dto
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoemSummaryDto {
  //Member
  private Long memberId;
  private String name;
  private String profileImage;

  //Poem
  private Long poemId;
  private Integer categoryId;
  private String title;
  private String content;
  private String description;
  private Integer view;
  private LocalDateTime writeDate;
  private List<MemberDto> bookmarkMemberList;
}
