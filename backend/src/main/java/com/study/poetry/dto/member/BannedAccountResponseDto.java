package com.study.poetry.dto.member;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
//사용 제한 계정정보 응답 dto
@Data
@Builder
public class BannedAccountResponseDto {
  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "password must not be null")
  private String email;

  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "password must not be null")
  private LocalDateTime restrictionEndDate;
}
