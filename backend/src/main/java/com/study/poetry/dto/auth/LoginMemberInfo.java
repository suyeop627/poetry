package com.study.poetry.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
//access(refresh) token 에 저장된 회원 정보
//@LoggedInUserInfo 어노테이션과 함께 사용될 경우, 토큰에 저장된 회원 정보를 저장함
//토큰 로그인 시, Authentication의 principal로 사용됨.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginMemberInfo {
  @NotNull
  private Long memberId;
  @NotNull
  private String email;
  @NotNull
  private String name;
  @NotNull
  private Set<String> roles;
  private LocalDateTime restrictionEndDate;
  public String getRestrictionEndDateOrEmptyString(){
    return restrictionEndDate != null ? getRestrictionEndDate().toString() : "";
  }
}
