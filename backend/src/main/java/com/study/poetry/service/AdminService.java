package com.study.poetry.service;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.entity.Category;
import com.study.poetry.exception.ResourceNotFoundException;
import com.study.poetry.repository.CategoryRepository;
import com.study.poetry.utils.enums.LogFileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//관리자 페이지 관련 요청 수행
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
  private final MemberService memberService;
  private final CategoryRepository categoryRepository;
  private final PoemService poemService;
  private final ReportService reportService;
  //관리자 페이지 접속 시 입력해야할 비밀번호
  @Value("${admin.password}")
  String adminPassword;

  //관리자 페이지 접속 시 입력한 비밀번호 일치여부 확인
  public boolean validateAdminPassword(String password) {
    return adminPassword.equals(password);
  }

  //카테고리 수정 사항 저장
  public List<Category> updateCategory(List<Category> categoryList, LoginMemberInfo loginMember) {
    log.info("Category updated by member (id: {})", loginMember.getMemberId());
    return categoryRepository.saveAll(categoryList);
  }

  //POETRY 메뉴 접속 시, 서비스 관련 정보 조회(회원 현황, 관리자 현황, 카테고리별 게시글 현황, 이용제한회원 현황)
  public Map<String, Object> getPoetryInfoData() {
    Map<String, Object> reportData = new HashMap<>();
    reportData.put("memberStatistics", memberService.getMemberStatistics());
    reportData.put("adminList", memberService.getAdminList());
    reportData.put("poemStatistics", poemService.getPoemStatistics());
    reportData.put("restrictedMemberList", reportService.getRestrictedMemberList());

    return reportData;
  }


  // 주어진 디렉토리에서 파일검색
  //해당 파일들을 압축하여 하나의 zip 파일로 만듦
  //압축된 데이터는 ByteArrayOutputStream에 저장
  //ByteArrayOutputStream의 내용을 바이트 배열로 반환
  public byte[] getZipFileOfLogs(String searchDirectory) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
        Path folder = Paths.get(searchDirectory);
        try (Stream<Path> paths = Files.walk(folder)) {
          paths
              .filter(Files::isRegularFile)
              .forEach(file -> addFileToZip(file, zipOutputStream));
        }
        zipOutputStream.finish();
      }

      byteArrayOutputStream.close();
      return byteArrayOutputStream.toByteArray();
    } catch (
        NoSuchFileException e) {
      throw new ResourceNotFoundException(e.getMessage());
    } catch (
        IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  //ZipOutputStream 각 로그파일 저장
  private void addFileToZip(Path file, ZipOutputStream zipOut) {
    try {
      String fileName = file.getFileName().toString();
      zipOut.putNextEntry(new ZipEntry(fileName));
      Files.copy(file, zipOut);
      zipOut.closeEntry();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  //로그파일 목록 조회시 정렬 방식
  //yyyy_MM_dd_filename(number).log 의 형식에서,
  //1순위 yyyy_mm_dd
  //2순위 filename
  //3순위 (number) 순으로 정렬
  public int compareLogFileName(String o1, String o2) {
    String[] parts1 = o1.split("_");
    String[] parts2 = o2.split("_");

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    try {
      Date date1 = dateFormat.parse(parts1[0] + parts1[1] + parts1[2]);
      Date date2 = dateFormat.parse(parts2[0] + parts2[1] + parts2[2]);
      if (date1.compareTo(date2) != 0) {
        return date1.compareTo(date2);
      } else if (parts1[3].compareTo(parts2[3]) != 0) {
        return parts1[3].compareTo(parts2[3]);
      } else {
        int num1 = extractNumber(o1);
        int num2 = extractNumber(o2);
        return Integer.compare(num1, num2);
      }
    } catch (ParseException e) {
      log.error(e.getMessage());
      return 0;
    }
  }

  //로그파일 명에서 괄호안의 숫자 추출
  private int extractNumber(String fileName) {
    int startIndex = fileName.indexOf("(");
    int endIndex = fileName.indexOf(")");
    if (startIndex != -1 && endIndex != -1) {
      String numberString = fileName.substring(startIndex + 1, endIndex);
      return Integer.parseInt(numberString);
    } else {
      return 0;
    }
  }

  //로그파일 목록 조회
  //yyyy_mm 폴더 안의 failed_to_delete 로그파일 목록과 internal_server_error 로그파일목록을 각 list에 담아 map으로 반환
  public Map<String, List<String>> getLogFileList(String searchDirectory) {

    try (Stream<Path> paths = Files.walk(Paths.get(searchDirectory))) {
      Map<String, List<String>> logFileMap = new HashMap<>();
      List<String> failedToDeleteFileLogs = new ArrayList<>();
      List<String> internalServerErrorLogs = new ArrayList<>();

      paths
          .filter(Files::isRegularFile)
          .map(path ->
              path.toString()
                  .replace("\\", "/")//searchDirectory는 경로가 /로 구분돼지만 path는 \로 구분되어, /로 변경 후 처리
                  .replace(searchDirectory + "/", "")
          ).sorted(this::compareLogFileName)
          .forEach(
              logFile -> {
                if (logFile.contains(LogFileType.FAILED_TO_DELETE_FILE.getFileName().replace(".log",""))) {
                  failedToDeleteFileLogs.add(logFile);
                } else {
                  internalServerErrorLogs.add(logFile);
                }
              }
          );

      logFileMap.put("failedToDeleteFileLogs", failedToDeleteFileLogs);
      logFileMap.put("internalServerErrorLogs", internalServerErrorLogs);

      return logFileMap;
    } catch (IOException e) {
      //해당경로에 폴더가 없으면 예외 발생
      File file = new File(searchDirectory);
      if (!file.exists()) {
        throw new ResourceNotFoundException(e.getMessage());
      } else {
        throw new RuntimeException(e.getMessage());
      }
    }
  }
}