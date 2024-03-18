package com.study.poetry.repository;

import com.study.poetry.entity.Poem;
import com.study.poetry.entity.PoemViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoemViewLogRepository extends JpaRepository<PoemViewLog, Long> {
  boolean existsByPoemAndMemberIpAddress(Poem poem, String UserIpAddr);
  boolean existsByPoemAndMemberId(Poem poem, Long memberId);

}
