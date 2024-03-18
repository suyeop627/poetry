package com.study.poetry.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
//애플리케이션에서 발생하는 에러를 클라이언트로 전달하기 위한 응답 dto
@Data
@Builder
@AllArgsConstructor
public class ErrorDto {
  String path;
  String message;
  int statusCode;
  LocalDateTime localDateTime;
}
