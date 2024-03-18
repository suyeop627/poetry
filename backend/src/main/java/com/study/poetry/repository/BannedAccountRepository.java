package com.study.poetry.repository;

import com.study.poetry.entity.BannedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BannedAccountRepository extends JpaRepository<BannedAccount, Long> {
  boolean existsByEmail(String email);

  BannedAccount findByEmail(String email);

  long countByRestrictionEndDateBefore(LocalDateTime now);

  void deleteByRestrictionEndDateBefore(LocalDateTime now);
}
