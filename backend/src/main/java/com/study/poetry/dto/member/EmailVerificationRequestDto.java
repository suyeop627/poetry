package com.study.poetry.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
//이메일 인증 코드 확인 요청 dto
@Data
public class EmailVerificationRequestDto {
  @Email(message = "must be a well-formed email address")
  @NotNull(message = "email must not be null")
  private String email;

  @Size(min=6,max = 6, message = "verifyCode size must be 6")
  @NotNull(message = "verifyCode must not be null")
  private String verifyCode;
}
