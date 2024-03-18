package com.study.poetry.repository;

import com.study.poetry.entity.Bookmark;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.Poem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
  void deleteByPoem(Poem poem);
  void deleteByMember(Member member);
  Page<Bookmark> findByMember_MemberId(Long memberId, PageRequest pageRequest);

  boolean existsByMember_MemberIdAndPoem(Long memberId, Poem poem);

  List<Bookmark> findByPoemOrderByBookmarkDate(Poem poem);

  void deleteByPoemIn(List<Poem> poemsByMember);

  void deleteByMember_MemberIdAndPoem_PoemId(Long memberId, Long poemId);
}
