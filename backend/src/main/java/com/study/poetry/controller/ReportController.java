package com.study.poetry.controller;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.dto.report.ReportRequestDto;
import com.study.poetry.dto.report.ReportResultSaveRequestDto;
import com.study.poetry.dto.report.ReportSummaryInterface;
import com.study.poetry.dto.utils.ReportPageSearchDto;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.Report;
import com.study.poetry.exception.ResourceDuplicatedException;
import com.study.poetry.service.MemberService;
import com.study.poetry.service.ReportService;
import com.study.poetry.utils.WebUtils;
import com.study.poetry.utils.annotation.TokenToMemberInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reports")
public class ReportController {
  private final ReportService reportService;
  private final MemberService memberService;

  //게시글 기신고 여부 조회 요청 처리
  @GetMapping("{poemId}/check")
  public ResponseEntity<?> checkAlreadyReported(@TokenToMemberInfo LoginMemberInfo loginMemberInfo,
                                                @PathVariable("poemId") Long poemId) {
    boolean reportedByMemberId = reportService.isReportedByMemberId(loginMemberInfo, poemId);
    if (reportedByMemberId) {
      throw new ResourceDuplicatedException(
          "Member(id: %d) is reported poem(id: %d) already".formatted(loginMemberInfo.getMemberId(), poemId));
    }
    return ResponseEntity.ok().build();
  }

  //게시글 신고 요청 처리
  @PostMapping
  public ResponseEntity<?> report(@Valid @RequestBody ReportRequestDto reportRequestDto,
                                  BindingResult bindingResult,
                                  @TokenToMemberInfo LoginMemberInfo loginMemberInfo,
                                  HttpServletRequest request) {

    log.info("member(id: {}) is attempting to report poem(id : {})",
        loginMemberInfo.getMemberId(), reportRequestDto.getPoemId());

    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    Member member = memberService.getMemberByIdOrThrow(loginMemberInfo.getMemberId());

    Report savedReport = reportService.report(member, reportRequestDto);

    URI createdUri = WebUtils.getCreatedUri(savedReport.getReportId());
    log.info("The report from member {} to poem {} saved successfully. Created Uri: {}",
        loginMemberInfo.getMemberId(), savedReport.getReportId(), createdUri);

    return ResponseEntity.created(createdUri).build();
  }

  //신고 내역 목록 조회 요청 처리
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getReportList(@ModelAttribute ReportPageSearchDto reportPageSearchDto) {

    Page<ReportSummaryInterface> reportSummaryPage = reportService.getReportList(reportPageSearchDto);
    log.info("Retrieved {} reports in page {}. Total pages: {}, Total poems: {}.",
        reportSummaryPage.getNumberOfElements(), reportPageSearchDto.getPage(),
        reportSummaryPage.getTotalPages(), reportSummaryPage.getTotalElements());

    return ResponseEntity.ok().body(reportSummaryPage);
  }

  //신고 상세 정보 조회 요청 처리
  @GetMapping("{reportId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getReportDetails(@PathVariable Long reportId,
                                            @TokenToMemberInfo LoginMemberInfo loginMember) {
    log.info("Admin (id:{}) requests report details for report id {}.", loginMember.getMemberId(), reportId);
    return ResponseEntity.ok().body(reportService.getReportDetails(reportId, loginMember));
  }

  //신고 처리내역 저장 요청 처리
  @PatchMapping("{reportId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> applyWithReportResult(@RequestBody ReportResultSaveRequestDto reportResultSaveRequestDto, BindingResult bindingResult,
                                                 @PathVariable Long reportId,
                                                 @TokenToMemberInfo LoginMemberInfo loginMemberInfo,
                                                 HttpServletRequest request,
                                                 HttpSession session) {
    log.info("Admin (id: {}) requests save report result for report(id: {}). reportResult : {}.",
        loginMemberInfo.getMemberId(), reportId, reportResultSaveRequestDto);

    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    reportService.applyReportResult(reportId, reportResultSaveRequestDto, loginMemberInfo, session);
    return ResponseEntity.ok().body(reportResultSaveRequestDto);
  }

  //신고 내역 삭제 요청 처리
  @DeleteMapping("{reportId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> deleteReport(@PathVariable Long reportId,
                                        @TokenToMemberInfo LoginMemberInfo loginMemberInfo,
                                        HttpSession session) {

    log.info("Admin (id: {}) requests deleting report(id: {}).",
        loginMemberInfo.getMemberId(), reportId);

    reportService.deleteReport(reportId, session);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/members/{adminId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> countByDoneByMemberId(@PathVariable Long adminId) {
    return ResponseEntity.ok(reportService.countByDoneByMemberId(adminId));
  }
}
