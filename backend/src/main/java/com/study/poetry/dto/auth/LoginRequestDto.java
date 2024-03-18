package com.study.poetry.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
//회원 로그인 정보 요청 dto
@Data
@AllArgsConstructor
public class LoginRequestDto {
  @Email(message = "must be a well-formed email address")
  @NotNull(message = "email must not be null")
  private String email;
  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "password must not be null")
  private String password;
}
