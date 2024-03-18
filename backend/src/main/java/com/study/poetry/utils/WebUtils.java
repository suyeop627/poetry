package com.study.poetry.utils;

import com.study.poetry.dto.error.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WebUtils {
  //controller에서 바인딩 된 에러가 있을 경우 처리
  public static ResponseEntity<Set<ErrorDto>> getErrorResponseFromBindingResult(BindingResult bindingResult, HttpServletRequest request) {
    if (bindingResult.hasErrors()) {
      List<FieldError> fieldErrors = bindingResult.getFieldErrors();
      Set<ErrorDto> errorDtoSet = fieldErrors.stream()
          .map(error -> new ErrorDto(getHttpMethodAndURI(request), error.getDefaultMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()))
          .collect(Collectors.toSet());
      return ResponseEntity.badRequest().body(errorDtoSet);
    }
    return null;
  }

  //자원 생성 후, 자원 생성 uri 반환
  public static URI getCreatedUri(Long id) {
    return ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();
  }

  //클라이언트 ip 반환
  public static String getClientAddr(HttpServletRequest request) {
    // 각 헤더를 반복하여 처리하는 반복문
    String[] headersToCheck = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED"};

    String ipAddress = null;

    for (String header : headersToCheck) {
      ipAddress = request.getHeader(header);
      if (isValidIpAddress(ipAddress)) {
        break;
      }
    }

    // 유효한 IP 주소가 없으면 기본값으로 remoteAddr 사용
    if (!isValidIpAddress(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }

    System.out.println("Client's IP Address: " + ipAddress);
    return ipAddress;
  }

  private static boolean isValidIpAddress(String ipAddress) {
    return ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress);
  }

  public static String getHttpMethodAndURI(HttpServletRequest request){
    return request.getMethod() + " " + request.getRequestURI();
  }
}
