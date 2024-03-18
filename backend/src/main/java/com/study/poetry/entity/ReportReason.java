package com.study.poetry.entity;

import com.study.poetry.utils.enums.ReportReasonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//신고 사유 저장 엔티티
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReportReason {
  @Id
  @Column(name="report_reason_id")
  private Long reportReasonId;

  @Column
  @Enumerated(EnumType.STRING)
  private ReportReasonType reportReason;
}