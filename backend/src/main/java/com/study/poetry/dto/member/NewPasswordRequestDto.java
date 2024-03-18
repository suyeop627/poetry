package com.study.poetry.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
//비밀번호 분실 시, 새 비밀번호 발급 요청 dto
@Data
public class NewPasswordRequestDto {
  @Email(message = "must be a well-formed email address")
  @NotNull(message = "email must not be null")
  private String email;

  @Size(min = 8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "new password must not be null")
  private String newPassword;
}
