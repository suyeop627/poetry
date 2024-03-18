package com.study.poetry.dto.report;

import com.study.poetry.utils.enums.ReportReasonType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
//신고 요청 dto
@Data
public class ReportRequestDto {
  @NotNull(message = "poemId must not be null")
  private Long poemId;

  @NotNull(message = "reportReasonTypes must not be null")
  private List<ReportReasonType> reportReasonTypes;

  @NotNull(message = "reportComment must not be null")
  @Size(max = 50, message = "reportComment size must be under 30")
  private String reportComment;
}
