package com.study.poetry.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

//MemberSignupRequestDto와 MemberUpdateRequestDto의 조상 클래스
//기본 회원 정보를 담당하며, email 및 phone 중복확인 메서드의 파라미터로 사용
@Data
public class SimpleMemberInfoDto {
  @Email(message = "must be a well-formed email address")
  @NotNull(message = "email must not be null")
  private String email;

  @Size(min=2, max=16, message = "name size must be between 2 and 16")
  @NotNull(message = "name must not be null")
  private String name;

  @Pattern(regexp ="^01(?:0|1|[6-9])\\d{7,8}$", message = "must be well-formed phone number")
  @NotNull(message = "phone must not be null")
  private String phone;
}
