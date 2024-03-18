package com.study.poetry.service;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.auth.LoginRequestDto;
import com.study.poetry.dto.auth.LoginResponseDto;
import com.study.poetry.dto.auth.ReissueAccessTokenRequestDto;
import com.study.poetry.dto.member.BannedAccountResponseDto;
import com.study.poetry.entity.BannedAccount;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.RefreshToken;
import com.study.poetry.exception.JwtAuthenticationException;
import com.study.poetry.exception.ResourceNotFoundException;
import com.study.poetry.jwt.JwtExceptionType;
import com.study.poetry.jwt.JwtUtils;
import com.study.poetry.repository.BannedAccountRepository;
import com.study.poetry.security.MemberDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

//사용자 인증(로그인 및 토큰) 관련 처리 담당 클래스.
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  private final BannedAccountRepository bannedAccountRepository;
  private final String TYPE_ACCESS = "ACCESS";
  private final String TYPE_REFRESH = "REFRESH";

  //로그인 처리
  //로그인 성공 시, 회원 정보가 저장된 LoginResponseDto 반환
  public LoginResponseDto login(LoginRequestDto loginRequestDto) {

    LoginMemberInfo loginMemberInfo = getAuthenticatedLoginMemberInfo(loginRequestDto);
    System.out.println("loginMemberInfo = " + loginMemberInfo);

    deleteRefreshTokenIfExists(loginRequestDto.getEmail());

    String accessToken = null;
    Long refreshTokenId = null;

    //로그인 제한일이 존재하지 않을 경우에만 토큰 생성
    if (loginMemberInfo.getRestrictionEndDateOrEmptyString().isEmpty()) {

      accessToken = jwtUtils.issueAuthToken(loginMemberInfo, TYPE_ACCESS);
      log.info("Access token for member(memberId: {}) generated. ", loginMemberInfo.getMemberId());

      String refreshToken = jwtUtils.issueAuthToken(loginMemberInfo, TYPE_REFRESH);
      log.info("Refresh token for member(memberId: {}) generated.", loginMemberInfo.getMemberId());

      RefreshToken refreshTokenOfLoginMember = saveRefreshTokenOfLoginMember(loginMemberInfo.getMemberId(), refreshToken);
      refreshTokenId = refreshTokenOfLoginMember.getRefreshTokenId();

      log.info("Authentication success for the user with email. Email: {}", loginRequestDto.getEmail());
    } else {
      log.info("Member (id:{}) is restricted. Pass creating token.", loginMemberInfo.getMemberId());
    }

    return LoginResponseDto.builder()
        .name(loginMemberInfo.getName())
        .memberId(loginMemberInfo.getMemberId())
        .accessToken(accessToken)
        .refreshTokenId(refreshTokenId)
        .roles(loginMemberInfo.getRoles())
        .restrictionEndDate(loginMemberInfo.getRestrictionEndDateOrEmptyString())
        .build();
  }

  //loginRequestDto을 사용하여 인증 성공 시, LoginMemberInfo 을 반환함.
  private LoginMemberInfo getAuthenticatedLoginMemberInfo(LoginRequestDto loginRequestDto) {

    Authentication authentication = generateAuthenticationFromLoginRequestDto(loginRequestDto);

    //Member entity를 필드로 가지는 MemberDetails를 principal로 사용함.
    MemberDetails principal = (MemberDetails) authentication.getPrincipal();

    Member member = principal.getMember();

    LocalDateTime restrictionEndDate = getRestrictionEndDateIfExist(member);

    log.info("Authentication for member(memberId: {}) generated. Authentication type: {}, principal: {}",
        member.getMemberId(), authentication.getClass(), authentication.getPrincipal());

    return LoginMemberInfo.builder()
        .memberId(member.getMemberId())
        .roles(member.getRoleNameSet())
        .email(member.getEmail())
        .name(member.getName())
        .restrictionEndDate(restrictionEndDate)
        .build();
  }

  //email과 password로 인증 처리 후, Authentication 반환.
  private Authentication generateAuthenticationFromLoginRequestDto(LoginRequestDto loginRequestDto) {
    //첫 로그인 시, username과 password로 해당유저가 존재하는지 확인하므로, security가 기본으로 제공하는 UsernamePasswordAuthenticationToken 사용함
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getEmail(), loginRequestDto.getPassword())
    );
  }


  //로그인 시 기존에 저장돼있는 refresh token이 존재할 경우 삭제처리.
  protected void deleteRefreshTokenIfExists(String email) {
    refreshTokenService
        .findRefreshTokenByMemberEmail(email)
        .ifPresent(refreshToken -> refreshTokenService.deleteRefreshTokenById(refreshToken.getRefreshTokenId()));
  }

  //로그인 요청 회원에 사용 제한일이 존재할 경우, 제한 종료일 반환
  private LocalDateTime getRestrictionEndDateIfExist(Member member) {
    LocalDateTime restrictionEndDate = null;
    if (member.getMemberRestriction() != null) {
      restrictionEndDate = member.getMemberRestriction().getRestrictionEndDate();
    }
    return restrictionEndDate;
  }



  //로그인 시, 인증이 성공된 회원의 refresh token을 db에 저장.
  private RefreshToken saveRefreshTokenOfLoginMember(Long memberId, String refreshToken) {
    Claims claims = jwtUtils.extractClaimsFromRefreshToken(refreshToken);
    Date expirationDate = claims.getExpiration();
    LocalDateTime expiration = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    RefreshToken refreshTokenOfLoginMember =
        RefreshToken.builder()
            .token(refreshToken)
            .memberId(memberId)
            .expiredAt(expiration)
            .build();
    refreshTokenService.saveRefreshToken(refreshTokenOfLoginMember);
    return refreshTokenOfLoginMember;
  }


  //refresh token을 사용해서 access token 재발행 및 loginResponseDto 반환
  @Transactional
  public LoginResponseDto reAuthenticateWithRefreshToken(ReissueAccessTokenRequestDto reissueAccessTokenRequestDto) {

    RefreshToken refreshToken = findRefreshTokenOrThrow(reissueAccessTokenRequestDto);

    Claims claimsFromRefreshToken = getClaimsOfRefreshTokenOrDelete(refreshToken.getToken());

    LoginMemberInfo loginMemberInfo = generateLoginMemberInfoFromClaims(claimsFromRefreshToken);

    String accessToken = jwtUtils.issueAuthToken(loginMemberInfo, TYPE_ACCESS);

    log.info("Access token for member(memberId: {}) re-generated from Refresh token. Access token : {}", loginMemberInfo.getMemberId(), accessToken);
    return LoginResponseDto.builder()
        .name(loginMemberInfo.getName())
        .memberId(loginMemberInfo.getMemberId())
        .accessToken(accessToken)
        .refreshTokenId(refreshToken.getRefreshTokenId())
        .roles(loginMemberInfo.getRoles())
        .build();
  }

//  //Access token 재발행시 사용할 refresh token 조회
  private RefreshToken findRefreshTokenOrThrow(ReissueAccessTokenRequestDto reissueAccessTokenRequestDto) {
    return refreshTokenService
        .findRefreshTokenByMemberIdAndTokenId(
            reissueAccessTokenRequestDto.getMemberId(), reissueAccessTokenRequestDto.getRefreshTokenId())
        .orElseThrow(() -> new ResourceNotFoundException("The refresh token does not exist in the database."));
  }



  //refresh token에서 추출한 클레임을 반환 전달받은 refresh token의 유효성 문제가 있는 경우, 해당 토큰을 db에서 삭제.
  private Claims getClaimsOfRefreshTokenOrDelete(String refreshToken) {
    Claims claimsOfRefreshToken = null;
    try {
      claimsOfRefreshToken = jwtUtils.extractClaimsFromRefreshToken(refreshToken);

    } catch (ExpiredJwtException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.EXPIRED_REFRESH_TOKEN, refreshToken);
    } catch (NullPointerException | IllegalArgumentException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.TOKEN_NOT_FOUND, refreshToken);
    } catch (MalformedJwtException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.INVALID_TOKEN, refreshToken);
    } catch (SignatureException e) {
      deleteRefreshTokenAndThrow(JwtExceptionType.INVALID_SIGNATURE, refreshToken);
    } catch (Exception e) {
      //전달받은 토큰을 parsing 할때 기타 예외가 발생한 경우 기존 토큰 삭제 및 예외 처리
      log.error("Unspecified exception occurred when parsing the token");
      deleteRefreshTokenAndThrow(JwtExceptionType.UNKNOWN_ERROR, refreshToken);
    }
    return claimsOfRefreshToken;
  }

  //Claims에서 회원정보를 추출하여, LoginMemberInfo 반환
  private LoginMemberInfo generateLoginMemberInfoFromClaims(Claims claimsFromRefreshToken) {
    List<String> roleFromClaims = (List<String>) claimsFromRefreshToken.get("roles");

    return LoginMemberInfo.builder()
        .memberId(claimsFromRefreshToken.get("memberId", Long.class))
        .email(claimsFromRefreshToken.getSubject())
        .name(claimsFromRefreshToken.get("name", String.class))
        .roles(new HashSet<>(roleFromClaims))
        .build();
  }

  //전달받은 refresh token을 db에서 삭제하고, JwtException을 던짐
  private void deleteRefreshTokenAndThrow(JwtExceptionType jwtExceptionType, String refreshToken) {
    log.error(jwtExceptionType.getMessage());
    refreshTokenService.deleteRefreshTokenByToken(refreshToken);
    throw new JwtAuthenticationException(jwtExceptionType.getMessage(), jwtExceptionType);
  }


  //사용이 제한된 이메일로 로그인 시도시 bannedAccountResponseDto 를 담은 response entity(forbidden)  반환
  public ResponseEntity<BannedAccountResponseDto> responseForbiddenIfBannedAccount(String email) {
    if (bannedAccountRepository.existsByEmail(email)) {
      BannedAccount bannedAccount = bannedAccountRepository.findByEmail(email);
      BannedAccountResponseDto bannedAccountResponseDto =
          BannedAccountResponseDto.builder()
              .restrictionEndDate(bannedAccount.getRestrictionEndDate())
              .email(bannedAccount.getEmail())
              .build();

      log.info("Email:{} requesting login is banned account. Login login procedure passed.", email);

      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(bannedAccountResponseDto);
    }
    return null;
  }
}
