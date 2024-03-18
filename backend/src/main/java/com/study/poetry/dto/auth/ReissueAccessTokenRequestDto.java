package com.study.poetry.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

//Access token 재발행 요청 dto
@Data
public class ReissueAccessTokenRequestDto {
  @NotNull(message = "refreshTokenId must be not null")
  Long refreshTokenId;
  @NotNull(message = "memberId must be not null")
  Long memberId;
}
