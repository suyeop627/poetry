package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
//신고별 상세 내역 정보 저장 엔티티
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reportDetailsId;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "report_Details_report_reason",
      joinColumns = @JoinColumn(name = "report_details_id"),
      inverseJoinColumns = @JoinColumn(name = "report_reason_id")
  )
  @Builder.Default
  private Set<ReportReason> reportReasons = new HashSet<>();

  @Column(length = 100)
  private String reportComment;

  @CreationTimestamp
  private LocalDateTime reportDate;

  @ManyToOne
  @JoinColumn(name = "report_id")
  private Report report;
}
