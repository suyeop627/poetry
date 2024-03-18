package com.study.poetry.repository;

import com.study.poetry.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
  Optional<RefreshToken> findByMemberIdAndRefreshTokenId(Long memberId, Long refreshTokenId);
  @Query("SELECT r FROM RefreshToken r JOIN Member m ON m.memberId=r.memberId WHERE m.email = :email")
  Optional<RefreshToken> selectRefreshTokenByMemberEmail(@Param("email") String email);

  void deleteByMemberId(Long memberId);
  void deleteByToken(String token);
  void deleteByExpiredAtBefore(LocalDateTime now);
  long countByExpiredAtBefore(LocalDateTime now);

}
