package com.study.poetry.dto.report;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

//신고 처리 결과 저장 요청 dto
@Data
public class ReportResultSaveRequestDto {
  @NotNull(message = "categoryId must not be null")
  private Integer categoryId;

  @NotNull(message = "backgroundDeletion must not be null")
  private boolean backgroundDeletion;

  @NotNull(message = "reportComment must not be null")
  private boolean deleted;

  @NotNull(message = "profileImageDeletion must not be null")
  private boolean profileImageDeletion;

  @NotNull(message = "writerName must not be null")
  @Size(max = 16, message = "writerName size must be under 30")
  private String writerName;

  private Long memberRestrictionAddDate;

  @Size(max = 100, message = "restrictionReason size must be under 30")
  private String restrictionReason;
}
