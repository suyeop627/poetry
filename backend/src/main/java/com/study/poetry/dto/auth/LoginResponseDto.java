package com.study.poetry.dto.auth;

import lombok.*;

import java.util.Set;

//회원 로그인 성공시 응답 dto
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter//getter 없으면 HttpMediaTypeNotAcceptableException 발생
public class LoginResponseDto {
  String accessToken;
  Long refreshTokenId;
  Long memberId;
  String name;
  Set<String> roles;
  String restrictionEndDate;
}
