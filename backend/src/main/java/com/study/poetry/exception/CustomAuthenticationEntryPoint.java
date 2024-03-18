package com.study.poetry.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.utils.LoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import static com.study.poetry.utils.WebUtils.getHttpMethodAndURI;

//Spring Security의 filter에서 발생한 AuthenticationException 처리 클래스
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;
  @Value("${jwt.exception.response.header}")
  private String JWT_EXCEPTION_HEADER;

  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
    log.error("Entry point accessed : AuthenticationException occurred. ");

    if (authException instanceof DisabledException disabledException) {
      log.error(disabledException.getMessage());
      setResponseContentTypeAndStatusCode(response, HttpServletResponse.SC_FORBIDDEN);

    } else if (authException instanceof JwtAuthenticationException jwtException) {
      log.error(jwtException.getJwtExceptionType().getMessage());
      response.setHeader(JWT_EXCEPTION_HEADER, jwtException.getJwtExceptionType().getStatus());
      setResponseContentTypeAndStatusCode(response, HttpServletResponse.SC_UNAUTHORIZED);

    } else {
      log.error(authException.getMessage());
      setResponseContentTypeAndStatusCode(response, HttpServletResponse.SC_UNAUTHORIZED);
    }
    ErrorDto errorDto = createErrorDto(request, response, authException);
    LoggingUtils.loggingErrorDto(this.getClass().getSimpleName(), errorDto);

    try (PrintWriter writer = response.getWriter()) {
      objectMapper.writeValue(writer, errorDto);
    }
  }

  private void setResponseContentTypeAndStatusCode(HttpServletResponse response, int scUnauthorized) {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(scUnauthorized);
  }

  private static ErrorDto createErrorDto(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AuthenticationException authenticationException) {
    return ErrorDto.builder()
        .localDateTime(LocalDateTime.now())
        .message(authenticationException.getMessage())
        .path(getHttpMethodAndURI(request))
        .statusCode(response.getStatus())
        .build();
  }
}
