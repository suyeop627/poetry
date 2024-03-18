package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//이메일 인증시 인증번호 저장 엔티티
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailVerificationCode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 20)
  private String code;

  @Column(length = 100)
  private String email;

  @Column
  private LocalDateTime expiredAt;
}
