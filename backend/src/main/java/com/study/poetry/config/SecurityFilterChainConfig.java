package com.study.poetry.config;

import com.study.poetry.exception.CustomAuthenticationEntryPoint;
import com.study.poetry.jwt.JwtAuthenticationFilter;
import com.study.poetry.jwt.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;
//Security filter chain 설정 클래스
@Configuration
//@EnableWebSecurity(debug = true)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityFilterChainConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  public SecurityFilterChainConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                                   JwtAuthenticationProvider jwtAuthenticationProvider,
                                   CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {

    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
  }


// SecurityFilterChain의 보안 구성을 정의함.
// csrf 비활성화 - jwt 사용시, stateless한 세션을 사용하므로, csrf의 위험성이 낮음<
// cors 기본값 사용
// formLogin, HttpBasic 인증 비활성화
// Http 요청 권한 설정
// 세션 설정(STATELESS)
// JWT 인증 filter 및 provider 추가
// 인증 예외처리 설정
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(httpRequest ->
            httpRequest
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .requestMatchers(HttpMethod.POST, "/members", "/auth","/members/email/*", "/members/verifyCode")
                .permitAll()
                .requestMatchers(HttpMethod.PUT, "/auth","/members/password")
                .permitAll()
                .requestMatchers(HttpMethod.DELETE, "/auth/*","/members/*/restricted")
                .permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/mainContent",
                    "/members/*/profileImage/*",
                    "/category",
                    "/poems/*/backgroundImage/*",
                    "/poems",
                    "/poems/*",
//                    "/error", - 토큰 관련 예외 발생시 스프링의 기본 에러 처리설정으로 진행하려하지만, /error로 매핑한게 없어서 500에러 발생-> entrypoint 직접호출하기로.
                    "/poems/members/*",
                    "/poems/bookmark/*")
                .permitAll()
                .anyRequest()
                .authenticated()

        ).sessionManagement(securitySessionManagementConfigurer -> securitySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authenticationProvider(jwtAuthenticationProvider)
        .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(customAuthenticationEntryPoint));

    return http.build();
  }


}
