package com.study.poetry.utils.annotation;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.jwt.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenToMemberInfoArgumentResolver implements HandlerMethodArgumentResolver {

  //argumentResolver 적용할 파라미터 판단.
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(TokenToMemberInfo.class)
           && parameter.getParameterType() == LoginMemberInfo.class;
  }

  //SecurityContext의 Authentication의 principal을 가져와서, LoginMemberInfo 의 형태로 반환
  @Override
  public Object resolveArgument(MethodParameter parameter,
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest,
                                WebDataBinderFactory binderFactory) {

    try {
      JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
      if (jwtAuthenticationToken == null) return null;

      Object principal = getPrincipalFromToken(jwtAuthenticationToken);
      if (principal == null) return null;

      LoginMemberInfo loginMemberInfo = getLoginMemberInfoFromPrincipal(jwtAuthenticationToken, principal);

      log.info("LoginMemberInfo generated from access token. Logged-in member: {}", loginMemberInfo);
      return loginMemberInfo;

    } catch (Exception e) {
      log.error("Failed to resolve login member information.", e);
      throw new BadCredentialsException("Failed to resolve login member information", e);
    }
  }

  //SecurityContext에서 Authentication을 가져와서 JwtAuthenticationToken으로 변환하여 반환
  private JwtAuthenticationToken getJwtAuthenticationToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //permitAll()인 uri로 접근할 경우, authentication은 AnonymousAuthenticationToken로 저장되어 따로 분류함.
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      log.warn("Anonymous user tried to access.");
      return null;
    }
    return (JwtAuthenticationToken) authentication;
  }

  //JwtAuthenticationToken에서 Principal을 추출하여 반환
  private Object getPrincipalFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
    Object principal = jwtAuthenticationToken.getPrincipal();
    if (principal == null) {
      log.warn("Principal is null in JwtAuthenticationToken.");
      return null;
    }
    return principal;
  }

  //Principal을 LoginMemberInfo로 변환하여 반환
  private LoginMemberInfo getLoginMemberInfoFromPrincipal(JwtAuthenticationToken jwtAuthenticationToken, Object principal) {
    LoginMemberInfo loginMemberInfo = (LoginMemberInfo) principal;
    setRolesToLoginMemberInfo(jwtAuthenticationToken, loginMemberInfo);
    return loginMemberInfo;
  }

  //LoginMemberInfo에 역할 추가
  private void setRolesToLoginMemberInfo(JwtAuthenticationToken jwtAuthenticationToken, LoginMemberInfo loginMemberInfo) {
    Collection<GrantedAuthority> authorities = jwtAuthenticationToken.getAuthorities();
    Set<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    loginMemberInfo.setRoles(roles);
  }


}
