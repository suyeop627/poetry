package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//서비스 이용 제한 이후 탈퇴한 계정 정보 저장 엔티티. (이용 제한일 이전 재가입 방지)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BannedAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bannedAccountId;

  @Column(length = 100)
  private String email;

  @Column(length = 200)
  private String restrictionReason;

  @Column
  private LocalDateTime restrictionStartDate;

  @Column
  private LocalDateTime restrictionEndDate;

}
