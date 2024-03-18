package com.study.poetry.jwt;

import com.study.poetry.exception.JwtAuthenticationException;
import com.study.poetry.utils.WebUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//Jwt 인증 처리 필터
//요청 헤더에 토큰이 없는 경우 - 남아있는 필터로 넘어감
//요청 헤더에 토큰이 있는 경우 - AuthenticationProvider를 통해 인증 처리
//인증 실패시 AuthenticationEntryPoint 를 호출하여, 인증 실패 응답.
//
@RequiredArgsConstructor
@Configuration
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final RequestAttributeSecurityContextRepository requestAttributeSecurityContextRepository;


  // jwt 관련 예외가 발생하더라도 POST /error로 리다이렉트 함.
  // '/error'에 리다이렉트 될 경우, Authentication 이 ANONYMOUS 로 지정되어, '항상' InsufficentAuthetnication 발생
  // 예외 응답을 세분화 하기 위해, jwt 관련 에러 발생 시에는 entrypoint에서 commence를 직접 호출
  private final AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    log.info("JwtAuthenticationFilter activated");
    request.setAttribute("ipAddress", WebUtils.getClientAddr(request));
    //로그아웃 요청 시, refreshToken을 헤더에 담아 요청을 보내므로, 필터를 통과시킴. 여기서는 accessToken 을 사용하여 인증 처리.
    //=> refresh token은 db에 저장하고, id를 받아서 삭제하도록 변경
//    if(request.getMethod().equals(HttpMethod.DELETE.name()) && request.getRequestURI().equals("/auth")){
//      log.info("JwtAuthenticationFilter passed. Logout called with refresh token. Token : {}", request.getHeader("Authorization"));
//      filterChain.doFilter(request, response);
//      return;
//    }
    try {
      String token = getTokenFromRequest(request);
      if(token==null){
        log.info("Request header does not contains token, proceeding remaining filters");
        filterChain.doFilter(request, response);
        return;
      }

      JwtAuthenticationToken unAuthenticatedToken = new JwtAuthenticationToken(token);
      log.info("Attempting to obtain Authentication by JwtAuthenticationProvider");
      //principal -> memberInfoDto
      JwtAuthenticationToken authenticatedToken = jwtAuthenticationProvider.authenticate(unAuthenticatedToken);
      log.info("Authentication for member(memberId: {}) generated. Authentication type: {}, principal: {}",
          authenticatedToken.getMemberId(),authenticatedToken.getClass(), authenticatedToken.getPrincipal());


      SecurityContext context = SecurityContextHolder.getContext();
      context.setAuthentication(authenticatedToken);
      requestAttributeSecurityContextRepository.saveContext(context, request, response);//SseEmitter에서 발생하는 AccessDeniedException 방지


      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      callAuthenticationEntryPoint(request, response, JwtExceptionType.EXPIRED_ACCESS_TOKEN);

    } catch (NullPointerException  | IllegalArgumentException e) {
      callAuthenticationEntryPoint(request, response, JwtExceptionType.TOKEN_NOT_FOUND);

    } catch (MalformedJwtException e) {
      callAuthenticationEntryPoint(request, response, JwtExceptionType.INVALID_TOKEN);

    } catch (SignatureException e){
      callAuthenticationEntryPoint(request, response, JwtExceptionType.INVALID_SIGNATURE);

    } catch(DisabledException e){
      callAuthenticationEntryPoint(request, response, e);
    }catch (Exception e){
      log.error("Unspecified exception occurred while parsing the token", e);
      e.printStackTrace();
      //callAuthenticationEntryPoint(request, response, JwtExceptionType.UNKNOWN_ERROR);
    }
  }



  //요청 헤더에 토큰 유무를 확인하여, null 또는 토큰을 반환.
  private String getTokenFromRequest(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return null;
    }
    return authorizationHeader.split(" ")[1];
  }
  //토큰 파싱과정에서 예외가 발생한 경우, AuthenticationEntryPoint를 호출하여, 에외 처리 응답을 위임함.
  private void callAuthenticationEntryPoint(HttpServletRequest request,
                                            HttpServletResponse response,
                                            JwtExceptionType jwtExceptionType) throws IOException, ServletException {
    log.info("Exception occurred in {}. Call entry point", this.getClass().getSimpleName());
    JwtAuthenticationException exception = new JwtAuthenticationException(jwtExceptionType);
    callAuthenticationEntryPoint(request, response, exception);
  }
  private void callAuthenticationEntryPoint(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException exception) throws ServletException, IOException {
    authenticationEntryPoint.commence(request, response, exception);
  }
}
