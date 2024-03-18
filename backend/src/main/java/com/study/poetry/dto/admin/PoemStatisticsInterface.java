package com.study.poetry.dto.admin;

//관리자 메뉴의 Poetry 페이지의 카테고리별 게시글 현황 정보 응답 dto
public interface PoemStatisticsInterface {
 Integer getCategoryId();
 String getCategoryName();
 Long getTotalPoemsCount();
 Long getPoemsTodayCount();
 Long getPoemsLastMonthCount();
 Long getPoemsLast3MonthsCount();
 Long getPoemsLast6MonthsCount();
 Long getPoemsLastYearCount();
}