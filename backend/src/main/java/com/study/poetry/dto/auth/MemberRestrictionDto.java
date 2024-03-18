package com.study.poetry.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
//회원 서비스 이용 제한 정보 응답 dto
@Data
@Builder
public class MemberRestrictionDto {
  private Long memberId;
  private String email;
  private String name;
  private LocalDateTime restrictionStartDate;
  private LocalDateTime restrictionEndDate;
  private String restrictionReason;
}
