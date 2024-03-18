package com.study.poetry.exception;

import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.jwt.JwtExceptionType;
import com.study.poetry.utils.LoggingUtils;
import com.study.poetry.utils.enums.LogFileType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.study.poetry.utils.WebUtils.getHttpMethodAndURI;

//Controller 에서 발생하는 예외 처리 클래스
@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
  @Value("${jwt.exception.response.header}")
  private String JWT_EXCEPTION_HEADER;

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorDto> handleException(BadCredentialsException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
  }

  @ExceptionHandler(InsufficientAuthenticationException.class)
  public ResponseEntity<ErrorDto> handleException(InsufficientAuthenticationException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.FORBIDDEN.value());
    return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ResourceDuplicatedException.class)
  public ResponseEntity<ErrorDto> handleException(ResourceDuplicatedException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.CONFLICT.value());
    return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorDto> handleException(ResourceNotFoundException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
  }
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDto> handleException(BadRequestException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorDto> handleException(JwtException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());
    if(e instanceof ExpiredJwtException){
      return createExpiredJwtResponse(errorDto, JwtExceptionType.EXPIRED_ACCESS_TOKEN);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
  }

  @ExceptionHandler(JwtAuthenticationException.class)
  public ResponseEntity<ErrorDto> handleException(JwtAuthenticationException e, HttpServletRequest request){
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.UNAUTHORIZED.value());
    if(e.getJwtExceptionType()==JwtExceptionType.EXPIRED_REFRESH_TOKEN){
      return createExpiredJwtResponse(errorDto, JwtExceptionType.EXPIRED_REFRESH_TOKEN);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorDto> handleException(AccessDeniedException e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.FORBIDDEN.value());
    return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
  }
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleException(Exception e, HttpServletRequest request) {
    ErrorDto errorDto = createErrorDto(request, e, HttpStatus.INTERNAL_SERVER_ERROR.value());

    writeExceptionOnLogFile(e, request);

    return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  //ErrorDto 생성
  //ErrorDto : 에러 관련 사항 요약 제공하기위한 dto
  private ErrorDto createErrorDto(HttpServletRequest request, Exception e, int errorCode){
    ErrorDto errorDto = ErrorDto.builder()
        .path(getHttpMethodAndURI(request))
        .message(e.getMessage())
        .localDateTime(LocalDateTime.now())
        .statusCode(errorCode)
        .build();
    log.error(e.getMessage());
    if(log.isDebugEnabled()){
      e.printStackTrace();
    }
    LoggingUtils.loggingErrorDto(this.getClass().getSimpleName(), errorDto);
    return errorDto;
  }
  //jwt가 만료된 경우, jwtexception 헤더를 추가하여 반환
  private ResponseEntity<ErrorDto> createExpiredJwtResponse(ErrorDto errorDto, JwtExceptionType jwtExceptionType) {
    log.error("{}. Exception code attached to header. JwtException: {}", jwtExceptionType.getMessage(), jwtExceptionType.getStatus());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .header(JWT_EXCEPTION_HEADER, jwtExceptionType.getStatus()) //EXPIRED_ACCESS_TOKEN or EXPIRED_REFRESH_TOKEN
        .body(errorDto);
  }

  //분류되지 않은 예외 발생 시 로그파일에 기록
  private void writeExceptionOnLogFile(Exception e, HttpServletRequest request) {
    String httpMethodAndURI = getHttpMethodAndURI(request);
    String content = "\nRequest : " + httpMethodAndURI + "\n" + e.getMessage();
    List<String> stackWithLineBreak = Arrays.stream(e.getStackTrace()).map(stack -> stack + "\n").toList();
    content += "\n" + stackWithLineBreak.toString().replace(",","");
    LoggingUtils.logToFile(LogFileType.INTERNAL_SERVER_ERROR, content);
  }
}
