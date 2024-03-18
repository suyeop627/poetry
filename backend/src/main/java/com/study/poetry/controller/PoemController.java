package com.study.poetry.controller;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.dto.poem.PoemSaveRequestDto;
import com.study.poetry.dto.poem.PoemSettingsSaveRequestDto;
import com.study.poetry.dto.poem.PoemSummaryDto;
import com.study.poetry.dto.utils.PageParameterDto;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.Poem;
import com.study.poetry.service.MemberService;
import com.study.poetry.service.PoemService;
import com.study.poetry.utils.WebUtils;
import com.study.poetry.utils.annotation.TokenToMemberInfo;
import com.study.poetry.utils.enums.ImageType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("poems")
@Slf4j
public class PoemController {
  private final PoemService poemService;
  private final MemberService memberService;

  //글작성 요청 처리
  @PostMapping
  public ResponseEntity<?> writePoem(@Valid @RequestPart(value = "poem") PoemSaveRequestDto poemSaveRequestDto,
                                     BindingResult bindingResultOfPoem,
                                     @Valid @RequestPart(value = "poemSettings") PoemSettingsSaveRequestDto poemSettingsSaveRequestDto,
                                     BindingResult bindingResultOfPoemSettings,
                                     @RequestPart(value = "backgroundImage", required = false) MultipartFile backgroundImage,
                                     @TokenToMemberInfo LoginMemberInfo loginMember,
                                     HttpServletRequest request,
                                     HttpSession session) {

    ResponseEntity<Set<ErrorDto>> errorDtoSetOfPoem = getErrorDtoSetIfExists(bindingResultOfPoem, bindingResultOfPoemSettings, request);
    if (errorDtoSetOfPoem != null) return errorDtoSetOfPoem;

    log.info("Member (id: {}) requests save poem with {}", loginMember.getMemberId(), poemSaveRequestDto);

    poemSaveRequestDto.setMember(memberService.getMemberByIdOrThrow(loginMember.getMemberId()));
    Poem savedPoem = poemService.savePoemAndPoemSettings(poemSaveRequestDto, poemSettingsSaveRequestDto, backgroundImage, session);
    URI createdUri = WebUtils.getCreatedUri(savedPoem.getPoemId());
    log.info("The poem requested save is created on {}", createdUri);
    return ResponseEntity.created(createdUri).build();
  }

  //글 목록 조회 요청 처리
  @GetMapping
  public ResponseEntity<Page<PoemSummaryDto>> getPoems(@ModelAttribute PageParameterDto pageParameterDto) {
    Page<PoemSummaryDto> poemListResponseDtoPage = poemService.getPoems(pageParameterDto);
    log.info("Retrieved {} poems in page {}. Total pages: {}, Total poems: {}.",
        poemListResponseDtoPage.getNumberOfElements(), pageParameterDto.getPage(),
        poemListResponseDtoPage.getTotalPages(), poemListResponseDtoPage.getTotalElements());

    return ResponseEntity.ok().body(poemListResponseDtoPage);
  }

  //글 상세 정보 조회 요청 처리
  @GetMapping("/{poemId}")
  public ResponseEntity<?> getPoem(@PathVariable Long poemId,
                                   @TokenToMemberInfo LoginMemberInfo loginMember,
                                   HttpServletRequest request) {
    return ResponseEntity.ok().body(poemService.getPoem(poemId, loginMember, request));
  }

  //회원이 작성한 게시글 목록 조회요청 처리
  @GetMapping("/members/{memberId}")
  public ResponseEntity<Page<PoemSummaryDto>> getPoemsByMemberId(@PathVariable Long memberId,
                                                                 @ModelAttribute PageParameterDto pageParameterDto) {
    Page<PoemSummaryDto> poemListResponseDtoPage = poemService.getPoemsByMember(memberId, pageParameterDto);
    log.info("Retrieved {} poems in page {}. Total pages: {}, Total poems: {}.",
        poemListResponseDtoPage.getNumberOfElements(), pageParameterDto.getPage(),
        poemListResponseDtoPage.getTotalPages(), poemListResponseDtoPage.getTotalElements());

    return ResponseEntity.ok().body(poemListResponseDtoPage);
  }

  //배경이미지 조회 요청 처리
  @GetMapping("/{poemId}/backgroundImage/{backgroundImageName}")
  public ResponseEntity<?> getProfileImage(@PathVariable("poemId") Long poemId,
                                           @PathVariable("backgroundImageName") String backgroundImageName,
                                           HttpSession session,
                                           HttpServletResponse response) throws IOException {
    return poemService.getBackgroundImageResponse(poemId, backgroundImageName, ImageType.POEM_BACKGROUND, session, response);
  }

  //글 삭제 요청 처리
//ROLE_ADMIN : 모든 회원의 글 정보 수정 가능
//ROLE_USER : 전달받은 poem의 작성자 id와 access token에 담긴 memberId가 일치하는 경우에만 삭제 가능
  @DeleteMapping("/{poemId}")
  public ResponseEntity<?> deletePoem(@PathVariable("poemId") Long poemId,
                                      @TokenToMemberInfo LoginMemberInfo loginMember,
                                      HttpSession session) {

    log.info("Poem (id:{}) is requested for deletion from Member id:{}.", poemId, loginMember.getMemberId());

    Poem poem = poemService.findPoemByIdOrThrow(poemId);
    memberService.isAdminOrMemberOwnOrThrow(poem.getMember().getMemberId(), loginMember);
    poemService.deletePoem(poem, session);
    return ResponseEntity.ok().build();
  }

  //글 수정요청 처리
  //ROLE_ADMIN : 모든 회원의 글 정보 수정 가능
  //ROLE_USER : 전달받은 poem의 작성자 id와 access token에 담긴 memberId가 일치하는 경우에만 수정 가능
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @PutMapping("/{poemId}")
  public ResponseEntity<?> updatePoem(@PathVariable("poemId") Long poemId,
                                      @Valid @RequestPart(value = "poem") PoemSaveRequestDto poemSaveRequestDto,
                                      BindingResult bindingResultOfPoem,
                                      @Valid @RequestPart(value = "poemSettings") PoemSettingsSaveRequestDto poemSettingsSaveRequestDto,
                                      BindingResult bindingResultOfPoemSettings, @RequestPart(value = "backgroundImage", required = false) MultipartFile backgroundImage,
                                      @TokenToMemberInfo LoginMemberInfo loginMember,
                                      HttpServletRequest request,
                                      HttpSession session) {

    ResponseEntity<Set<ErrorDto>> errorDtoSetOfPoem = getErrorDtoSetIfExists(bindingResultOfPoem, bindingResultOfPoemSettings, request);
    if (errorDtoSetOfPoem != null) return errorDtoSetOfPoem;

    log.info("Poem (id:{}) is requested for update from Member id:{}.", poemId, loginMember.getMemberId());

    Poem savedPoem = poemService.findPoemByIdOrThrow(poemId);
    memberService.isAdminOrMemberOwnOrThrow(savedPoem.getMember().getMemberId(), loginMember);

    poemSaveRequestDto.setMember(memberService.getMemberByIdOrThrow(loginMember.getMemberId()));

    poemService.updatePoemAndPoemSettings(savedPoem, poemSaveRequestDto, poemSettingsSaveRequestDto, backgroundImage, session);

    return ResponseEntity.ok().build();
  }

  //게시글 및 게시글 설정 저장 및 수정 요청의 각 필드 유효성 검사
  private ResponseEntity<Set<ErrorDto>> getErrorDtoSetIfExists(BindingResult bindingResultOfPoem, BindingResult bindingResultOfPoemSettings, HttpServletRequest request) {
    ResponseEntity<Set<ErrorDto>> errorDtoSetOfPoem = WebUtils.getErrorResponseFromBindingResult(bindingResultOfPoem, request);
    if (errorDtoSetOfPoem != null) {
      return errorDtoSetOfPoem;
    }

    return WebUtils.getErrorResponseFromBindingResult(bindingResultOfPoemSettings, request);
  }

  //북마크 요청 처리
  @PostMapping("/{poemId}/bookmark")
  public ResponseEntity<?> bookmarkPoem(@PathVariable("poemId") Long poemId,
                                        @TokenToMemberInfo LoginMemberInfo loginMember) {
    Member member = memberService.getMemberByIdOrThrow(loginMember.getMemberId());
    poemService.bookmarkPoem(poemId, member);
    log.info("Member (id:{}) bookmark the poem (id:{})", loginMember.getMemberId(), poemId);
    return ResponseEntity.ok().build();
  }


  //북마크 취소 요청 처리
  @DeleteMapping("/{poemId}/bookmark")
  public ResponseEntity<?> cancelBookmarkPoem(@PathVariable("poemId") Long poemId,
                                        @TokenToMemberInfo LoginMemberInfo loginMember) {
    poemService.cancelBookmarkPoem(poemId, loginMember.getMemberId());
    log.info("Member (id:{}) cancel bookmarking the poem (id:{})", loginMember.getMemberId(), poemId);
    return ResponseEntity.ok().build();
  }


  //회원이 북마킹한 게시글 목록 조회 요청 처리
  @GetMapping("/bookmark/{memberId}")
  public ResponseEntity<?> getBookmarkPage(@PathVariable("memberId") Long memberId,
                                           @ModelAttribute PageParameterDto pageParameterDto) {
    Page<PoemSummaryDto> poemListResponseDtoPage = poemService.getBookmarkedPoemPage(memberId, pageParameterDto);

    log.info("Retrieved {} poems in page {}. Total pages: {}, Total poems: {}.",
        poemListResponseDtoPage.getNumberOfElements(), pageParameterDto.getPage(),
        poemListResponseDtoPage.getTotalPages(), poemListResponseDtoPage.getTotalElements());

    return ResponseEntity.ok().body(poemListResponseDtoPage);
  }

}
