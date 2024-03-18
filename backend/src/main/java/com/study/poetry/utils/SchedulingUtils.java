package com.study.poetry.utils;

import com.study.poetry.repository.BannedAccountRepository;
import com.study.poetry.repository.EmailVerificationCodeRepository;
import com.study.poetry.repository.MemberRestrictionRepository;
import com.study.poetry.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class SchedulingUtils {
  private final RefreshTokenRepository refreshTokenRepository;
  private final EmailVerificationCodeRepository emailVerificationCodeRepository;
  private final MemberRestrictionRepository memberRestrictionRepository;
  private final BannedAccountRepository bannedAccountRepository;

  public SchedulingUtils(RefreshTokenRepository refreshTokenRepository, EmailVerificationCodeRepository emailVerificationCodeRepository, MemberRestrictionRepository memberRestrictionRepository, BannedAccountRepository bannedAccountRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.emailVerificationCodeRepository = emailVerificationCodeRepository;
    this.memberRestrictionRepository = memberRestrictionRepository;
    this.bannedAccountRepository = bannedAccountRepository;
  }

  //매일 자정에 만료된 refreshToken 토큰 삭제
  @Scheduled(cron = "0 0 0 * * ?")
  public void deleteExpiredRefreshToken() {
    LocalDateTime now = LocalDateTime.now();
    long countOfExpiredRefreshToken = refreshTokenRepository.countByExpiredAtBefore(now);
    log.info("Method: deleteExpiredRefreshToken invoked. Count of expired Refresh token: {}", countOfExpiredRefreshToken);

    refreshTokenRepository.deleteByExpiredAtBefore(now);
  }

  // 매일 00시 5분에 자정기준 유효기간이 지난 메일 검증코드 삭제
  @Scheduled(cron = "0 5 0 * * ?")
  public void deleteExpiredVerificationCode() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
    long countOfVerificationCode = emailVerificationCodeRepository.countByExpiredAtBefore(now);
    log.info("Method: deleteExpiredVerificationCode invoked. Count of expired email verification code: {}", countOfVerificationCode);

    emailVerificationCodeRepository.deleteByExpiredAtBefore(now);
  }


  // 매일 00시 10분에 자정기준 제한 종료일이 지난 회원의 제한 해제
  @Scheduled(cron = "0 10 0 * * ?")
  public void deleteExpiredMemberRestriction() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
    long releasedMemberRestrictionCount = memberRestrictionRepository.countByRestrictionEndDateBefore(now);
    log.info("Method: deleteExpiredMemberRestriction invoked. Count of released member count: {}", releasedMemberRestrictionCount);

    memberRestrictionRepository.deleteByRestrictionEndDateBefore(now);
  }

  // 매일 00시 15에 자정기준 제한 종료일이 지난 사용불가 계정 삭제
  @Scheduled(cron = "0 15 0 * * ?")
  public void deleteExpiredBannedAccount() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
    long releasedBannedAccountCount = bannedAccountRepository.countByRestrictionEndDateBefore(now);
    log.info("Method: deleteExpiredBannedAccount invoked. Count of released account count: {}", releasedBannedAccountCount);

    bannedAccountRepository.deleteByRestrictionEndDateBefore(now);
  }
}
