package com.study.poetry.dto.admin;

//관리자 메뉴의 Poetry 페이지의 회원 현황 정보 응답 dto
public interface MemberStatisticsInterface {
  Long getTotalMembersCount();

  Long getNewMembersCountOfRecentMonth();

  Float getMonthlyNewMembersCount();
}
