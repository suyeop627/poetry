package com.study.poetry.service;

import com.study.poetry.dto.admin.MemberStatisticsInterface;
import com.study.poetry.dto.admin.PoemStatisticsInterface;
import com.study.poetry.dto.auth.MemberRestrictionDto;
import com.study.poetry.dto.member.MemberDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelDownloadService {

  //CellStyle 객체 저장 map
  final Map<String, CellStyle> cellStyleMap = new HashMap<>();
  private final String TITLE_CELL_STYLE_KEY = "title";
  private final String SUBTITLE_CELL_STYLE_KEY = "sub";
  private final String DATA_HEADER_CELL_STYLE_KEY = "dataHeader";
  private final String DATA_CELL_STYLE_KEY = "data";
  private final String DATA_FORMAT_DATE_CELL_STYLE_KEY = "dateData";
  private  final int COLUMN_START_INDEX = 1;
  private  final int COLUMN_END_INDEX = 7;
  private  final int COLUMN_WIDTH = 25*256;//256의 배수로 지정

  //엑셀 파일 생성
  public byte[] generatePoetryStatisticsExcelFile(Map<String, Object> reportData) throws IOException {

    Workbook workbook = new XSSFWorkbook();

    generateWorkSheet(workbook, reportData);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    workbook.write(outputStream);
    workbook.close();

    cellStyleMap.clear();
    return outputStream.toByteArray();
  }

  //worksheet 생성
  private void generateWorkSheet(Workbook workbook, Map<String, Object> data) {

    Sheet sheet = workbook.createSheet("Poetry Info");

    int rowNum = 0;
    rowNum = generateTitle(workbook, sheet, rowNum);
    rowNum = generateMemberInfo(workbook, data, sheet, rowNum);
    rowNum = generateAdminInfo(workbook, data, sheet, rowNum);
    rowNum = generatePoemInfo(workbook, data, sheet, rowNum);
    rowNum = generateRestrictedInfo(workbook, data, sheet, rowNum);

    applyBorderLayout(sheet, rowNum);
  }

  //title 셀 생성
  private int generateTitle(Workbook workbook, Sheet sheet, int rowNum) {
    CellStyle titleStyle = getTitleCellStyle(workbook);
    Row row = sheet.createRow(++rowNum);
    setStyleToRow(titleStyle, row);
    row.getCell(1).setCellValue("POE-TRY");
    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, COLUMN_START_INDEX, COLUMN_END_INDEX));
    return rowNum;
  }

  //subtitle 셀 생성
  private int generateSubTitle(String data, Workbook workbook, Sheet sheet, int rowNum) {
    //B~H 열 병합
    CellStyle subTitleStyle = getSubtitleCellStyle(workbook);
    Row row = sheet.createRow(++rowNum);
    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, COLUMN_START_INDEX, COLUMN_END_INDEX));
    setStyleToRow(subTitleStyle, row);
    row.createCell(1).setCellStyle(subTitleStyle);
    row.getCell(1).setCellValue(data);

    return rowNum;
  }
  //title 및 subtitle 셀 스타일 적용
  private void setStyleToRow(CellStyle cellStyle, Row row) {
    for(int i=COLUMN_START_INDEX; i<=COLUMN_END_INDEX; i++){
      row.createCell(i).setCellStyle(cellStyle);
    }
  }

  //회원 현황 영역 생성
  private int generateMemberInfo(Workbook workbook, Map<String, Object> data, Sheet sheet, int rowNum) {
    MemberStatisticsInterface memberStatistics = (MemberStatisticsInterface) data.get("memberStatistics");
    String subtitle = "회원 현황";
    rowNum = generateSubTitle(subtitle, workbook, sheet, rowNum);


    int[][] memberInfoLayout = new int[][]{{1, 1}, {2, 4}, {5, 7}};
    String[] memberDataHeaders = new String[]{"총 회원 수", "최근 1개월 가입자 수", "최근 1년 월평균 가입자 수"};
    rowNum = generateDataHeader(memberInfoLayout, memberDataHeaders, workbook, sheet, rowNum);

    rowNum = generateMemberData(memberInfoLayout, workbook, sheet, memberStatistics, rowNum);
    return rowNum;
  }

  //회원 현황 데이터 셀 생성
  private int generateMemberData(int[][] memberCellMergeLayout, Workbook workbook, Sheet sheet, MemberStatisticsInterface memberStatistics, int rowNum) {

    Row row = sheet.createRow(++rowNum);
    CellStyle dataStyle = getDataCellStyle(workbook);
    setStyleToRow(dataStyle, row);

    row.getCell(memberCellMergeLayout[0][0]).setCellValue(memberStatistics.getTotalMembersCount());
    row.getCell(memberCellMergeLayout[1][0]).setCellValue(memberStatistics.getNewMembersCountOfRecentMonth());
    row.getCell(memberCellMergeLayout[2][0]).setCellValue(memberStatistics.getMonthlyNewMembersCount());

    mergeCell(memberCellMergeLayout, sheet, rowNum);
    return rowNum;
  }

  //관리자 현황 영역 생성
  private int generateAdminInfo(Workbook workbook, Map<String, Object> data, Sheet sheet, int rowNum) {
    List<MemberDto> adminList = (List<MemberDto>) data.get("adminList");

    String subtitle = "관리자 현황 (%d 명)".formatted(adminList.size());
    rowNum = generateSubTitle(subtitle, workbook, sheet, rowNum);

    int[][] adminInfoLayout = new int[][]{{1, 1}, {2, 3}, {4, 5}, {6, 7}};
    String[] adminDataHeaders = new String[]{"관리자 번호", "이름", "이메일", "연락처"};
    rowNum = generateDataHeader(adminInfoLayout, adminDataHeaders, workbook, sheet, rowNum);

    rowNum = generateAdminListData(adminInfoLayout, workbook, sheet, adminList, rowNum);
    return rowNum;

  }

  //관리자 현황 데이터 셀 생성
  private int generateAdminListData(int[][] adminCellMergeLayout, Workbook workbook, Sheet sheet, List<MemberDto> adminList, int rowNum) {

    CellStyle dataStyle = getDataCellStyle(workbook);

    for (MemberDto admin : adminList) {
      Row row = sheet.createRow(++rowNum);
      setStyleToRow(dataStyle, row);

      row.getCell(adminCellMergeLayout[0][0]).setCellValue(admin.getMemberId());
      row.getCell(adminCellMergeLayout[1][0]).setCellValue(admin.getName());
      row.getCell(adminCellMergeLayout[2][0]).setCellValue(admin.getEmail());
      row.getCell(adminCellMergeLayout[3][0]).setCellValue(admin.getPhone());

      mergeCell(adminCellMergeLayout, sheet, rowNum);
    }
    return rowNum;
  }

  //각 섹션별 표 헤더 셀 생성
  private int generateDataHeader(int[][] mergeLayout, String[] headerNames, Workbook workbook, Sheet sheet, int rowNum) {
    CellStyle dataHeaderStyle = getDataHeaderCellStyle(workbook);
    Row row = sheet.createRow(++rowNum);
    for (int i = 0; i < headerNames.length; i++) {
      if (mergeLayout[i][0] < mergeLayout[i][1]) {
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, mergeLayout[i][0], mergeLayout[i][1]));
      }
      row.createCell(mergeLayout[i][0]).setCellValue(headerNames[i]);
      row.getCell(mergeLayout[i][0]).setCellStyle(dataHeaderStyle);
    }
    return rowNum;
  }

  //게시글 현황 영역 생성
  private int generatePoemInfo(Workbook workbook, Map<String, Object> data, Sheet sheet, int rowNum) {
    List<PoemStatisticsInterface> poemStatistics = (List<PoemStatisticsInterface>) data.get("poemStatistics");

    String subtitle = "카테고리별 신규 게시글 현황";
    rowNum = generateSubTitle(subtitle, workbook, sheet, rowNum);

    int[][] poemInfoLayout = new int[][]{{1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}};
    String[] poemDataHeaders = new String[]{"카테고리", "오늘", "최근 1개월", "최근 3개월", "최근 6개월", "최근 1년", "총"};
    rowNum = generateDataHeader(poemInfoLayout, poemDataHeaders, workbook, sheet, rowNum);

    rowNum = generatePoemData(poemInfoLayout, workbook, sheet, poemStatistics, rowNum);

    return rowNum;
  }

  //게시글 현황 데이터 셀 생성
  private int generatePoemData(int[][] poemCellHeaderDataLayout, Workbook workbook, Sheet sheet, List<PoemStatisticsInterface> poemStatistics, int rowNum) {
    CellStyle dataStyle = getDataCellStyle(workbook);
    for (PoemStatisticsInterface poemStatistic : poemStatistics) {
      Row row = sheet.createRow(++rowNum);
      setStyleToRow(dataStyle, row);
      row.getCell(poemCellHeaderDataLayout[0][0]).setCellValue(poemStatistic.getCategoryName());
      row.getCell(poemCellHeaderDataLayout[1][0]).setCellValue(poemStatistic.getPoemsTodayCount());
      row.getCell(poemCellHeaderDataLayout[2][0]).setCellValue(poemStatistic.getPoemsLastMonthCount());
      row.getCell(poemCellHeaderDataLayout[3][0]).setCellValue(poemStatistic.getPoemsLast3MonthsCount());
      row.getCell(poemCellHeaderDataLayout[4][0]).setCellValue(poemStatistic.getPoemsLast6MonthsCount());
      row.getCell(poemCellHeaderDataLayout[5][0]).setCellValue(poemStatistic.getPoemsLastYearCount());
      row.getCell(poemCellHeaderDataLayout[6][0]).setCellValue(poemStatistic.getTotalPoemsCount());

      mergeCell(poemCellHeaderDataLayout, sheet, rowNum);
    }
    return rowNum;
  }

  //이용제한 회원 현황 영역 생성
  private int generateRestrictedInfo(Workbook workbook, Map<String, Object> data, Sheet sheet, int rowNum) {
    List<MemberRestrictionDto> restrictedMemberList = (List<MemberRestrictionDto>) data.get("restrictedMemberList");

    String subtitle = "이용 제한 회원 현황(%d 명)".formatted(restrictedMemberList.size());
    rowNum = generateSubTitle(subtitle, workbook, sheet, rowNum);

    int[][] restrictedMemberLayout = new int[][]{{1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5},{6,7}};
    String[] restrictedMemberHeaderData = new String[]{"회원 번호", "이름", "이메일", "제한 시작일", "제한 종료일", "제한 사유"};
    rowNum = generateDataHeader(restrictedMemberLayout, restrictedMemberHeaderData, workbook, sheet, rowNum);

    rowNum = generateMemberRestrictionListData(restrictedMemberLayout, workbook, sheet, restrictedMemberList, rowNum);
    return rowNum;
  }

//이용 제한 회원 현황 데이터 셀 생성
  private int generateMemberRestrictionListData(int[][] restrictedMemberDataLayout, Workbook workbook, Sheet sheet, List<MemberRestrictionDto> restrictedMemberList, int rowNum) {
    CellStyle dateFormatStyle = getDateFormattedDataStyle(workbook);
    CellStyle dataStyle = getDataCellStyle(workbook);

    for (MemberRestrictionDto restrictedMember : restrictedMemberList) {
      Row row = sheet.createRow(++rowNum);

      setStyleToRow(dataStyle, row);
      row.getCell(restrictedMemberDataLayout[0][0]).setCellValue(restrictedMember.getMemberId());
      row.getCell(restrictedMemberDataLayout[1][0]).setCellValue(restrictedMember.getName());
      row.getCell(restrictedMemberDataLayout[2][0]).setCellValue(restrictedMember.getEmail());
      row.getCell(restrictedMemberDataLayout[3][0]).setCellValue(restrictedMember.getRestrictionStartDate());
      row.getCell(restrictedMemberDataLayout[4][0]).setCellValue(restrictedMember.getRestrictionEndDate());
      row.getCell(restrictedMemberDataLayout[5][0]).setCellValue(restrictedMember.getRestrictionReason());

      for (int i = 0; i < restrictedMemberDataLayout.length; i++) {
        if (restrictedMemberDataLayout[i][0] < restrictedMemberDataLayout[i][1]) {
          sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, restrictedMemberDataLayout[i][0], restrictedMemberDataLayout[i][1]));
        }
        if (i == 3 || i == 4) {
          row.getCell(restrictedMemberDataLayout[i][0]).setCellStyle(dateFormatStyle);
        }
      }
    }
    return rowNum;
  }

  //Cell style----------------------------------
  //title 셀스타일 생성 또는 조회
  private CellStyle getTitleCellStyle(Workbook workbook) {
    return cellStyleMap.computeIfAbsent(TITLE_CELL_STYLE_KEY,
        key -> {
          CellStyle titleStyle = workbook.createCellStyle();
          titleStyle.setAlignment(HorizontalAlignment.CENTER);
          titleStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
          titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
          titleStyle.setBorderBottom(BorderStyle.DOUBLE);

          Font titleFont = workbook.createFont();
          titleFont.setFontName("Malgun Gothic");
          titleFont.setBold(true);
          titleFont.setFontHeightInPoints((short) 45);
          titleStyle.setFont(titleFont);
          return titleStyle;
        });
  }
  //subTitle 셀스타일 생성 또는 조회
  private CellStyle getSubtitleCellStyle(Workbook workbook) {
    return cellStyleMap.computeIfAbsent(SUBTITLE_CELL_STYLE_KEY,
        key -> {
          CellStyle subTitleCellStyle = workbook.createCellStyle();
          subTitleCellStyle.setAlignment(HorizontalAlignment.CENTER);
          subTitleCellStyle.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex());
          subTitleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
          subTitleCellStyle.setBorderTop(BorderStyle.THIN);
          subTitleCellStyle.setBorderBottom(BorderStyle.THIN);


          Font subTitleFont = workbook.createFont();
          subTitleFont.setBold(true);
          subTitleFont.setFontHeightInPoints((short) 16);
          subTitleFont.setFontName("Malgun Gothic");
          subTitleCellStyle.setFont(subTitleFont);
          return subTitleCellStyle;
        });
  }

  //dataHeader 셀스타일 생성 또는 조회
  private CellStyle getDataHeaderCellStyle(Workbook workbook) {
    return cellStyleMap.computeIfAbsent(DATA_HEADER_CELL_STYLE_KEY,
        key -> {
          CellStyle dataHeaderStyle = workbook.createCellStyle();
          setDefaultDataCellStyle(dataHeaderStyle);
          Font font = workbook.createFont();
          font.setFontName("Malgun Gothic");
          font.setBold(true);
          dataHeaderStyle.setFont(font);
          return dataHeaderStyle;
        });
  }

  //data 셀스타일 생성 또는 조회
  private CellStyle getDataCellStyle(Workbook workbook) {
    return cellStyleMap.computeIfAbsent(DATA_CELL_STYLE_KEY,
        key -> {
          CellStyle dataStyle = workbook.createCellStyle();
          setDefaultDataCellStyle(dataStyle);
          Font font = workbook.createFont();
          font.setFontName("Malgun Gothic");
          dataStyle.setFont(font);
          return dataStyle;
        });
  }


  //data 날짜 형식 셀스타일 생성 또는 조회
  private CellStyle getDateFormattedDataStyle(Workbook workbook) {
    return cellStyleMap.computeIfAbsent(DATA_FORMAT_DATE_CELL_STYLE_KEY,
        key -> {
          CellStyle dataFormattedStyle = workbook.createCellStyle();
          setDefaultDataCellStyle(dataFormattedStyle);
          Font font = workbook.createFont();
          font.setFontName("Malgun Gothic");
          dataFormattedStyle.setFont(font);
          dataFormattedStyle
              .setDataFormat(
                  workbook
                      .getCreationHelper()
                      .createDataFormat()
                      .getFormat("yyyy-mm-dd"));
          return dataFormattedStyle;
        });
  }

  //dataHeader, data의 기본 셀스타일 지정
  private void setDefaultDataCellStyle(CellStyle cellStyle) {
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
  }

  //Layout -------------------------------------
  //각 주제별 영역의 레이아웃에 맞게 셀 병합 및 스타일 적용
  private void mergeCell(int[][] dataLayout, Sheet sheet, int rowNum) {
    for (int[] ints : dataLayout) {
      if (ints[0] < ints[1]) {
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, ints[0], ints[1]));
      }
    }
  }

  //표 전체영역 테두리 지정
  private void applyBorderLayout(Sheet sheet, int rowNum) {
    for (int i = COLUMN_START_INDEX; i <= COLUMN_END_INDEX; i++) {
      sheet.setColumnWidth(i, COLUMN_WIDTH);
    }
    CellRangeAddress region = new CellRangeAddress(1, rowNum, COLUMN_START_INDEX, COLUMN_END_INDEX);
    RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
    RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
    RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
    RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
  }
}