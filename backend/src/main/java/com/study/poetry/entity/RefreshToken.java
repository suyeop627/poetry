package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//리프레시 토큰 정보 저장 엔티티
@Entity
@Table(name="refresh_token")
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long refreshTokenId;

  @Column
  private Long memberId;

  @Column(length = 500)
  private String token;

  @Column
  private LocalDateTime expiredAt;
}
