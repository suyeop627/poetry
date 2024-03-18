package com.study.poetry.jwt;

import com.study.poetry.dto.auth.LoginMemberInfo;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
//Jwt를 사용하여 인증을 시도할 경우의 Authentication 객체
@Setter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
  private final Object principal;
  private String token;
//토큰만으로 인증 시도
  public JwtAuthenticationToken(String unAuthenticatedToken) {
    super(null);
    this.principal = null;
    this.token = unAuthenticatedToken;
    setAuthenticated(false);
  }
//토큰 + 사용자 정보 있음 ->Authentication
  public JwtAuthenticationToken(Object principal, String authenticatedToken, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.token = authenticatedToken;
    super.setAuthenticated(true);
  }
  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  public String getToken() {
    return token;
  }

  public Long getMemberId(){
    LoginMemberInfo principalOfAuthenticationToken = (LoginMemberInfo) principal;
    return principalOfAuthenticationToken.getMemberId();
  }

}