package com.study.poetry.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//신고 상세 조회 시, 게시글 관련 정보 응답 dto (ComprehensiveReportDto의 필드로 사용됨
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoemSummaryForReportResponseDto {
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
  //삭제 대기 상태로, 신고된 경우 신고내역삭제 전까지 삭제가 불가능하도록 지정하기위한 값
  private boolean deleted;
}
