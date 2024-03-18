package com.study.poetry.repository;

import com.study.poetry.dto.report.ReportSummaryInterface;
import com.study.poetry.dto.utils.ReportPageSearchDto;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.Poem;
import com.study.poetry.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
  boolean existsByReportDetails_Member_MemberIdAndPoem_PoemId(Long memberId, Long poemId);
  boolean existsByPoem_PoemId(Long poemId);
  Optional<Report> findByPoem_poemId(Long poemId);
  void deleteByPoem(Poem poem);
  @Query(value = """
      SELECT r.report_id                 AS reportId,
             creation_date               AS creationDate,
             report_status               AS reportStatus,
              case
                  when
                      report_status ='DONE' and m.name is null
                  then '탈퇴한관리자'
                  when
                      m.name is null
                  then '미정'

                else m.name
              end                        AS doneBy,
             COUNT(rd.report_id)         AS reportCount,
             p.title                     AS title,
             m2.name                     AS writer
      FROM report r
               LEFT JOIN report_details rd on r.report_id = rd.report_id
               JOIN poem p on r.poem_id = p.poem_id
               LEFT JOIN member m on r.done_by_member_id = m.member_id
               LEFT JOIN member m2 on p.member_id = m2.member_id
        WHERE 1=1
        AND (:#{#params.title} ='' OR p.title LIKE '%' :#{#params.title} '%')/*신고 대상 시 제목*/
        AND (:#{#params.content} ='' OR p.content LIKE '%' :#{#params.content} '%')/*신고 대상 시 내용*/
        AND (:#{#params.writer} ='' OR m2.name LIKE '%' :#{#params.writer} '%')/*신고대상 시 작성자 이름*/
        AND (:#{#params.name} ='' OR m.name LIKE '%' :#{#params.name} '%')/*관리자 이름*/
        AND (:#{#params.email} ='' OR m.email LIKE '%' :#{#params.email} '%')/*관리자 메일*/
        AND (:#{#params.reportStatus} ='' OR r.report_status LIKE '%' :#{#params.reportStatus} '%')/*신고 처리 상태*/
        AND (:#{#params.startDate} ='' OR r.creation_date >=:#{#params.startDate})
        AND (:#{#params.endDate} ='' OR r.creation_date <=DATE_ADD(:#{#params.endDate},INTERVAL 1 DAY))
        GROUP BY r.report_id
--        HAVING COUNT(rd.report_id )>0 신고자가 1명만 존재하고, 해당 회원이 탈퇴했을 경우, report는 존재하지만 count(rd.report_id)는 0인 상황 발생함.
        """, nativeQuery = true)
  Page<ReportSummaryInterface> selectReportSummary(@Param("params") ReportPageSearchDto params, PageRequest pageRequest);

  List<Report> findByDoneBy_MemberId(Long memberId);

  long countByDoneBy_MemberId(Long memberId);

  void deleteAllByPoem_Member(Member member);

  void deleteByPoemIn(List<Poem> poemListByRestrictedMember);
}
