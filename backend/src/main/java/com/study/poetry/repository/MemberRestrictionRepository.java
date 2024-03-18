package com.study.poetry.repository;

import com.study.poetry.entity.Member;
import com.study.poetry.entity.MemberRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRestrictionRepository extends JpaRepository<MemberRestriction, Long> {

  Optional<MemberRestriction> findByMember(Member member);

  void deleteByMember(Member member);

  long countByRestrictionEndDateBefore(LocalDateTime now);

  void deleteByRestrictionEndDateBefore(LocalDateTime now);
}
