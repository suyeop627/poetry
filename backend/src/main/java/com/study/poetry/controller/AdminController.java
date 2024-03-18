package com.study.poetry.controller;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.entity.Category;
import com.study.poetry.exception.ResourceNotFoundException;
import com.study.poetry.service.AdminService;
import com.study.poetry.service.ExcelDownloadService;
import com.study.poetry.utils.LoggingUtils;
import com.study.poetry.utils.annotation.TokenToMemberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
//관리자 페이지 관련 엔드포인트 담당 클래스
public class AdminController {
  private final AdminService adminService;
  private final ExcelDownloadService excelDownloadService;

  //관리자 페이지 접속 시, 비밀번호 확인
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("validatePassword")
  public ResponseEntity<?> validateAdminPassword(@RequestBody String password,
                                                 @TokenToMemberInfo LoginMemberInfo loginMember) {
    log.info("member (id: {}) is attempting to access admin page.", loginMember.getMemberId());
    if (adminService.validateAdminPassword(password)) {
      log.info("Admin password validation passed. member (id {}) confirmed to access admin page", loginMember.getMemberId());
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      log.error("Admin password validation failed. member (id {}) rejected to access admin page", loginMember.getMemberId());
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

  //카테고리 업데이트 요청 처리
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("category")
  public ResponseEntity<?> updateCategory(@RequestBody List<Category> categoryList,
                                          @TokenToMemberInfo LoginMemberInfo loginMember) {
    log.info("Admin member (id:{}) requested update categories to {}", loginMember.getMemberId(), categoryList);
    return ResponseEntity.ok(adminService.updateCategory(categoryList, loginMember));
  }

  //Poetry 현황 정보 조회 요청 처리
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("report")
  public ResponseEntity<?> getPoetryInfoData(@TokenToMemberInfo LoginMemberInfo loginMember) {
    log.info("Admin member (id:{}) requested get poetry information data.", loginMember.getMemberId());
    return ResponseEntity.ok(adminService.getPoetryInfoData());
  }

  //Poe-try 정보 엑셀 다운로드 요청 처리
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("downloadStatistics")
  public ResponseEntity<?> downloadPoetryInfo(@TokenToMemberInfo LoginMemberInfo loginMember) throws IOException {
    log.info("Admin member (id:{}) requested downloading excel for poe-try information.", loginMember.getMemberId());


    Map<String, Object> reportData = adminService.getPoetryInfoData();
    byte[] excelFile = excelDownloadService.generatePoetryStatisticsExcelFile(reportData);

    //헤더를 생성하는게 모듈화측면에서 더 나을 수 있음.
    HttpHeaders headers = new HttpHeaders();
    //엑셀파일임을 명시(MIME(Multipurpose Internet Mail Extensions) 타입을 명시해서 엑셀파임로 처리할 것을 알려줌
    headers.setContentType(
        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    //컨텐츠를 어떻게 처리할지 지정. attachment를 명시해서 리소스를 다운로드하도록 함
    headers.setContentDispositionFormData("attachment", "data.xlsx");
    //항상 서버에 검증받을 것을 명시함. 리소스 요청 전후로 캐시를 검사하지 않도록 지정(post/pre check-0)
    headers.setCacheControl("no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
    headers.setPragma("no-cache");
    return ResponseEntity.ok()
        .headers(headers)
        .body(excelFile);
  }


  //선택한 연월에 해당하는 로그파일 목록 조회 요청 처리
  @GetMapping("/logs/{searchMonth}")
  public ResponseEntity<?> getLogFiles(@PathVariable String searchMonth,
                                                  @TokenToMemberInfo LoginMemberInfo loginMember) {
    log.info("Admin member (id:{}) requested logs file list of {}.", loginMember.getMemberId(), searchMonth);

    String searchDirectory = LoggingUtils.LOG_DIRECTORY + searchMonth;

    Map<String, List<String>> logFiles = adminService.getLogFileList(searchDirectory);
    return ResponseEntity.ok(logFiles);

  }

  //선택한 로그파일 내용 조회 요청 처리
  @GetMapping("/logs/{searchMonth}/{fileName}")
  public ResponseEntity<String> getLogFileContent(@PathVariable String searchMonth,
                                                  @PathVariable String fileName,
                                                  @TokenToMemberInfo LoginMemberInfo loginMember) {

    String filePath = LoggingUtils.LOG_DIRECTORY + searchMonth + "/" + fileName;
    log.info("Admin member (id:{}) requested log file : {}.", loginMember.getMemberId(), filePath);
    try {
      String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
      return ResponseEntity.ok(fileContent);
    } catch (IOException e) {
      File file = new File(filePath);
      if (!file.exists()) {
        throw new ResourceNotFoundException(e.getMessage());
      } else {
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  //선택한 연월에 해당하는 로그 압축하일 다운로드 요청 처리
  @GetMapping("/logs/{searchMonth}/download")
  public ResponseEntity<byte[]> downloadLogFiles(@PathVariable String searchMonth,
                                                 @TokenToMemberInfo LoginMemberInfo loginMember) {

    log.info("Admin member (id:{}) requested download log files of {}.", loginMember.getMemberId(), searchMonth);

    String searchDirectory = LoggingUtils.LOG_DIRECTORY + searchMonth;

    byte[] logZipFile = adminService.getZipFileOfLogs(searchDirectory);

    String zipFileName = searchMonth + "_logs.zip";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/zip"));
    headers.setContentDispositionFormData("attachment", zipFileName);
    headers.setContentLength(logZipFile.length);

    return new ResponseEntity<>(logZipFile, headers, HttpStatus.OK);
  }
}
