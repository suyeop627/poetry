package com.study.poetry.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
//이메일 전송 실패할 경우
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnableToSendEmailException extends RuntimeException{
  public UnableToSendEmailException(String message) {
    super(message);
  }
}
