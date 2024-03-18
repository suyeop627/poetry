package com.study.poetry.dto.member;

import lombok.*;

import java.time.LocalDateTime;
//회원 가입 성공시, 응답 dto
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSignupResponseDto {
  private Long memberId;
  private String email;
  private String name;
  private LocalDateTime regdate;
}
