package com.study.poetry.entity;

import com.study.poetry.utils.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//신고 저장 엔티티
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Report {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reportId;

  @OneToOne
  @JoinColumn(name = "poem_id")
  private Poem poem;

  @Column
  @Enumerated(EnumType.STRING)
  private ReportStatus reportStatus;

  @CreationTimestamp
  private LocalDateTime creationDate;

  @ManyToOne
  private Member doneBy;

  @OneToMany(mappedBy = "report", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  @Builder.Default
  private List<ReportDetails> reportDetails = new ArrayList<>();

  public void addReportDetails(ReportDetails reportDetails){
    this.reportDetails.add(reportDetails);
  }
}
