package com.study.poetry.repository;

import com.study.poetry.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
  Optional<EmailVerificationCode> findByEmailAndCode(String email, String verifyCode);

  void deleteByEmail(String email);
  long countByExpiredAtBefore(LocalDateTime now);
  void deleteByExpiredAtBefore(LocalDateTime now);
}
