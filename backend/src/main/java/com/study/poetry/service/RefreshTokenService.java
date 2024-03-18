package com.study.poetry.service;

import com.study.poetry.entity.RefreshToken;
import com.study.poetry.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  //refresh token 저장
  public void saveRefreshToken(RefreshToken refreshToken) {
    log.info("Method: insertRefreshToken called with refreshTokenEntity: {}", refreshToken);
    refreshTokenRepository.save(refreshToken);
  }

  //memberId와 refreshTokenId 에 해당하는 refresh token 조회
  public Optional<RefreshToken> findRefreshTokenByMemberIdAndTokenId(Long memberId, Long refreshTokenId) {
    return refreshTokenRepository.findByMemberIdAndRefreshTokenId(memberId, refreshTokenId);
  }

  //회원 email에 해당하는 refresh token 조회
  public Optional<RefreshToken> findRefreshTokenByMemberEmail(String email) {
    return refreshTokenRepository.selectRefreshTokenByMemberEmail(email);
  }

  //refresth token id에 해당하는 값 삭제
  public void deleteRefreshTokenById(Long refreshTokenId) {
    refreshTokenRepository.deleteById(refreshTokenId);
  }

  //refresh token 만료시 호출되어, 해당 토큰 값에 해당하는 기존 refresh token 삭제.
  @Transactional
  public void deleteRefreshTokenByToken(String refreshToken) {
    refreshTokenRepository.deleteByToken(refreshToken);
  }
}
