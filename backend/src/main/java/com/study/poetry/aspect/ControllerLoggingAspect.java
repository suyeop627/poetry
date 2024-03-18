package com.study.poetry.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Aspect
@Component
@Slf4j
//컨트롤러 전역 로깅 클래스
public class ControllerLoggingAspect {
  private long startTime;
  //실행된 컨트롤러명, 패러미터 로깅 및 시작시간 저장
  @Before("execution(* com.study.poetry.controller.*.*(..))")
  public void logControllerMethodParameters(JoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    log.info("{}.{} called.", className, methodName);

    IntStream.range(0, args.length)
        .forEach(i -> log.info("--Param {}: {}", i, args[i]));

    startTime = System.currentTimeMillis();
  }

  //컨트롤러의 호출 종료 후 총 연산시간 로깅
  @After("execution(* com.study.poetry.controller.*.*(..))")
  public void logRequestEnd(JoinPoint joinPoint) {
    long endTime = System.currentTimeMillis();
    long elapsedTime = endTime - startTime;
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();

    log.info("{}.{} ended. Time taken: {} milliseconds", className, methodName, elapsedTime);
  }
}
