package com.study.poetry.repository;

import com.study.poetry.dto.admin.ReportDetailsStatisticsInterface;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.ReportDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportDetailsRepository extends JpaRepository<ReportDetails, Long> {
  List<ReportDetails> findByMember(Member member);
  @Query(value = """
      select sum(case when report_reason = 'MEMBER_PROFILE_IMAGE' then 1 else 0 end)     as memberProfileImageReportCount,
             sum(case when report_reason = 'POEM_CONTENT' then 1 else 0 end)             as poemContentReportCount,
             sum(case when report_reason = 'POEM_BACKGROUND_IMAGE' then 1 else 0 end)    as poemBackgroundImageReportCount,
             sum(case when report_reason = 'POEM_IRRELEVANT_CATEGORY' then 1 else 0 end) as poemIrrelevantCategoryReportCount,
             sum(case when report_reason = 'MEMBER_NAME' then 1 else 0 end)              as memberNameReportCount
      from report_details_report_reason rdrr
               join report_details rd on rdrr.report_details_id = rd.report_details_id
               join report_reason rr on rdrr.report_reason_id = rr.report_reason_id
      where rd.report_id = :reportId
      order by report_id""", nativeQuery = true)
  ReportDetailsStatisticsInterface selectStatisticsOfReportReasons(@Param("reportId") Long reportId);

  void deleteAllByMember(Member member);
}
