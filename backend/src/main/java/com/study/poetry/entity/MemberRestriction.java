package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
//로그인 제한 회원 정보 저장 엔티티
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRestriction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberRestrictionId;

  @OneToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @CreationTimestamp
  private LocalDateTime restrictionStartDate;

  @Column
  private LocalDateTime restrictionEndDate;

  @Column(length = 200)
  private String restrictionReason;
}
