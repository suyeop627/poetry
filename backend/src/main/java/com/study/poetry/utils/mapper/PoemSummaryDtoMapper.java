package com.study.poetry.utils.mapper;

import com.study.poetry.dto.member.MemberDto;
import com.study.poetry.dto.poem.PoemSummaryDto;
import com.study.poetry.entity.Bookmark;
import com.study.poetry.entity.Poem;
import com.study.poetry.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

//조회된 게시글을 PoemSummaryDto로 변환
@Component
@RequiredArgsConstructor
public class PoemSummaryDtoMapper implements Function<Poem, PoemSummaryDto> {
  private final BookmarkRepository bookmarkRepository;
  private final MemberDtoMapper memberDtoMapper;
  @Override
  public PoemSummaryDto apply(Poem poem) {
    return PoemSummaryDto.builder()
        .memberId(poem.getMember().getMemberId())
        .name(poem.getMember().getName())
        .profileImage(poem.getMember().getProfileImage())
        .poemId(poem.getPoemId())
        .categoryId(poem.getCategoryId())
        .title(poem.getTitle())
        .content(poem.getContent())
        .description(poem.getDescription())
        .view(poem.getViewLogs().size())
        .writeDate(poem.getWriteDate())
        .bookmarkMemberList(getBookmarkMemberList(poem))
        .build();
  }
  private List<MemberDto> getBookmarkMemberList(Poem poem){
    List<Bookmark> bookmarkList = bookmarkRepository.findByPoemOrderByBookmarkDate(poem);
    return  bookmarkList
        .stream()
        .map(bookmark -> memberDtoMapper.apply(bookmark.getMember()))
        .toList();
  }
}
