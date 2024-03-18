package com.study.poetry.dto.member;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
//회원 정보수정을 통한 비밀번호 변경 요청 dto
@Data
public class PasswordChangeDto {
  @Size(min = 8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "password must not be null")
  private String password;

  @Size(min = 8, max = 16, message = "newPassword size must be between 8 and 16")
  @NotNull(message = "new password must not be null")
  private String newPassword;

}
