package com.study.poetry.controller;

import com.study.poetry.dto.auth.LoginRequestDto;
import com.study.poetry.dto.auth.LoginResponseDto;
import com.study.poetry.dto.auth.ReissueAccessTokenRequestDto;
import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.dto.member.BannedAccountResponseDto;
import com.study.poetry.service.AuthenticationService;
import com.study.poetry.service.RefreshTokenService;
import com.study.poetry.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

//사용자 인증 클래스
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final RefreshTokenService refreshTokenService;


  //로그인 요청 처리
  @PostMapping
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto loginRequestDto,
                                 BindingResult bindingResult,
                                 HttpServletRequest request) {
    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    ResponseEntity<BannedAccountResponseDto> responseForbidden = authenticationService.responseForbiddenIfBannedAccount(loginRequestDto.getEmail());
    if (responseForbidden != null) return responseForbidden;

    log.info("Attempting authentication for the user with email. Email: {}", loginRequestDto.getEmail());

    LoginResponseDto response = authenticationService.login(loginRequestDto);
    return ResponseEntity.ok().body(response);
  }

  //access token 재 발급 요청 처리
  @PutMapping
  public ResponseEntity<?> reIssueAccessToken(@RequestBody @Valid ReissueAccessTokenRequestDto reissueAccessTokenRequestDto,
                                              BindingResult bindingResult,
                                              HttpServletRequest request) {

    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    log.info("Attempting to renew access token using the refresh token. Member id: {}",
        reissueAccessTokenRequestDto.getMemberId());

    LoginResponseDto response = authenticationService.reAuthenticateWithRefreshToken(reissueAccessTokenRequestDto);
    return ResponseEntity.ok().body(response);
  }


  //로그 아웃
  //db에 저장된 refresh token 삭제
  @DeleteMapping("/{refreshTokenId}")
//  public ResponseEntity<Void> logout(@TokenToMemberInfo LoginMemberInfo loggedInMember) {
  //access token 과 refresh token이 둘다 만료된 경우에서 로그아웃을 호출할 경우, 로그아웃을 위해 재인증을 받아야 함.->인층 없이 토큰을 전달받아 해당 토큰을 삭제하도록 변경
  public ResponseEntity<Void> logout(@PathVariable("refreshTokenId") Long refreshTokenId) {
    log.info("Logout called. Token id: {}", refreshTokenId);
    refreshTokenService.deleteRefreshTokenById(refreshTokenId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}