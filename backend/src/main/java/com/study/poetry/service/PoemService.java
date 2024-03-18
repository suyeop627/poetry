package com.study.poetry.service;

import com.study.poetry.dto.admin.PoemStatisticsInterface;
import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.poem.PoemSaveRequestDto;
import com.study.poetry.dto.poem.PoemSettingsSaveRequestDto;
import com.study.poetry.dto.poem.PoemSummaryDto;
import com.study.poetry.dto.utils.PageParameterDto;
import com.study.poetry.entity.*;
import com.study.poetry.exception.ResourceDuplicatedException;
import com.study.poetry.exception.ResourceNotFoundException;
import com.study.poetry.repository.*;
import com.study.poetry.utils.FileUtils;
import com.study.poetry.utils.enums.ImageType;
import com.study.poetry.utils.mapper.PoemSummaryDtoMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PoemService {
  private final PoemRepository poemRepository;
  private final PoemSettingsRepository poemSettingsRepository;
  private final FileUtils fileUtils;
  private final PoemSummaryDtoMapper poemSummaryDtoMapper;
  private final PoemViewLogRepository poemViewLogRepository;
  private final BookmarkRepository bookmarkRepository;
  private final NotificationService notificationService;
  private final ReportRepository reportRepository;
  private final String DEFAULT_SORT_FIELD = "writeDate";


  //게시글 및 설정 저장
  public Poem savePoemAndPoemSettings(PoemSaveRequestDto poemSaveRequestDto,
                                      PoemSettingsSaveRequestDto poemSettingsSaveRequestDto,
                                      MultipartFile backgroundImage,
                                      HttpSession session) {

    Poem poem = poemRequestToPoem(poemSaveRequestDto, null);

    PoemSettings poemSettings = poemSettingsRequestToPoemSettings(poemSettingsSaveRequestDto, null);

    Poem savedPoem = poemRepository.save(poem);

    String backgroundImageName = getBackgroundImageNameAfterSaveIfExists(backgroundImage, session, savedPoem);

    poemSettings.setBackgroundImage(backgroundImageName);
    PoemSettings savedPoemSettings = poemSettingsRepository.save(poemSettings);
    savedPoem.setPoemSettings(savedPoemSettings);

    return poemRepository.save(savedPoem);
  }

  //게시글 저장 시, 배경이미지 존재 시 파일 저장 및 이미지 이름 반환
  private String getBackgroundImageNameAfterSaveIfExists(MultipartFile backgroundImage, HttpSession session, Poem savedPoem) {
    String backgroundImageName = null;
    try {
      if (backgroundImage != null && !backgroundImage.isEmpty()) {
        backgroundImageName = fileUtils.uploadProfileImage(backgroundImage, savedPoem.getPoemId(), session, ImageType.POEM_BACKGROUND);
      }
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    return backgroundImageName;
  }

  //조건에 따라 조회된 Page<Poem>을 PoemSummaryDto로 변환하여 반환
  public Page<PoemSummaryDto> getPoems(PageParameterDto pageParameterDto) {
    int pageNumber = pageParameterDto.getPage() - 1; //Page는 pageNumber 0부터 시작이므로, 전달받은 page에서 -1

    PageRequest pageRequest =
        PageRequest.of(pageNumber, pageParameterDto.getSize(), Sort.by(Sort.Order.desc(DEFAULT_SORT_FIELD)));

    Page<Poem> poemPages;
    switch (pageParameterDto.getType()) {
      case "title" -> poemPages = poemRepository
          .findAllByCategoryIdAndDeletedIsFalseAndTitleContainingIgnoreCaseOrderByWriteDateDesc(pageParameterDto.getCategoryId(),
              pageParameterDto.getKeyword(),
              pageRequest);
      case "content" -> poemPages = poemRepository
          .findAllByCategoryIdAndDeletedIsFalseAndContentContainingIgnoreCaseOrderByWriteDateDesc(pageParameterDto.getCategoryId(),
              pageParameterDto.getKeyword(),
              pageRequest);
      case "writer" -> poemPages = poemRepository
          .findByCategoryIdAndDeletedIsFalseAndMember_NameContainingIgnoreCaseOrderByWriteDateDesc(pageParameterDto.getCategoryId(),
              pageParameterDto.getKeyword(),
              pageRequest);
      default -> poemPages = poemRepository.findAll(pageRequest);
    }
    return poemPages.map(poemSummaryDtoMapper);
  }

  //poemId에 해당하는 게시글 및 게시글 설정 조회
  public Map<String, Object> getPoem(Long poemId, LoginMemberInfo loginMember, HttpServletRequest request) {
    Poem poem = findPoemByIdOrThrow(poemId);

    saveViewLogsIfFirstView(poem, loginMember, request);

    Map<String, Object> poemAndPoemSettingsMap = new HashMap<>();
    poemAndPoemSettingsMap.put("poem", poemSummaryDtoMapper.apply(poem));
    poemAndPoemSettingsMap.put("poemSettings", poem.getPoemSettings());

    //로그인한 회원의 요청일 경우, 북마크 상태 조회
    if(loginMember!=null){
      poemAndPoemSettingsMap.put("isBookmarked", bookmarkRepository.existsByMember_MemberIdAndPoem(loginMember.getMemberId(), poem));
    }

    return poemAndPoemSettingsMap;
  }

  //poemId에 해당하는 게시글 조회
  public Poem findPoemByIdOrThrow(Long poemId) {
    return poemRepository.findById(poemId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format("poem id %s is not found", poemId)));
  }

  //게시글 조회시 조회수 증가(로그인 시 memberId기준, 비로그인시 ip기준)
  private void saveViewLogsIfFirstView(Poem poem, LoginMemberInfo loginMember, HttpServletRequest request) {
    if (loginMember == null) {
      String ipAddress = (String) request.getAttribute("ipAddress");
      if (!poemViewLogRepository.existsByPoemAndMemberIpAddress(poem, ipAddress)) {
        PoemViewLog poemViewLog = PoemViewLog.builder().poem(poem).memberIpAddress(ipAddress).build();
        poemViewLogRepository.save(poemViewLog);
      }
    } else {
      if (!poemViewLogRepository.existsByPoemAndMemberId(poem, loginMember.getMemberId())) {
        PoemViewLog poemViewLog = PoemViewLog.builder().poem(poem).memberId(loginMember.getMemberId()).build();
        poemViewLogRepository.save(poemViewLog);
      }
    }
  }

  //배경이미지 반환
  public ResponseEntity<?> getBackgroundImageResponse(Long poemId,
                                                      String backgroundImageName,
                                                      ImageType imageType,
                                                      HttpSession session,
                                                      HttpServletResponse response) throws IOException {
    return fileUtils.getImageResponse(poemId, backgroundImageName, imageType, session, response);
  }

  //게시글 삭제(배경이미지 존재시 배경이미지 삭제, 북마크 내역 삭제
  //신고된 게시글일 경우 deleted=true 설정. (논리적 삭제)
  @Transactional
  public void deletePoem(Poem poem, HttpSession session) {

    if (poem.getPoemSettings().getBackgroundImage() != null &&
        !poem.getPoemSettings().getBackgroundImage().isEmpty()) {
      String path = poem.getPoemId().toString();
      fileUtils.deleteImage(path, ImageType.POEM_BACKGROUND, session);
    }

    bookmarkRepository.deleteByPoem(poem);

    if (reportRepository.existsByPoem_PoemId(poem.getPoemId())) {
      poem.setDeleted(true);
      poemRepository.save(poem);
      return;
    }
    poemRepository.delete(poem);

  }
  //게시글 북마크 저장
  public void bookmarkPoem(Long poemId, Member member) {
    Poem poem = findPoemByIdOrThrow(poemId);
    checkBookmarkedOrThrow(poem, member.getMemberId());

    Bookmark bookmark =
        Bookmark.builder()
            .member(member)
            .poem(poem)
            .build();

    Bookmark savedBookmark = bookmarkRepository.save(bookmark);
    notificationService.saveBookmarkNotification(savedBookmark);
  }
  //북마크 삭제
  @Transactional
  public void cancelBookmarkPoem(Long poemId, Long memberId) {
    bookmarkRepository.deleteByMember_MemberIdAndPoem_PoemId(memberId, poemId);
  }

  //기존 북마크 한 게시글인지 확인
  private void checkBookmarkedOrThrow(Poem poem, Long memberId) {
    if (bookmarkRepository.existsByMember_MemberIdAndPoem(memberId, poem)) {
      throw new ResourceDuplicatedException(
          "The poem(id: %d) is already bookmarked by Member (id:%d)".formatted(memberId, poem.getPoemId()));
    }
  }

  //게시글 수정
  public void updatePoemAndPoemSettings(Poem savedPoem,
                                        PoemSaveRequestDto poemSaveRequestDto,
                                        PoemSettingsSaveRequestDto poemSettingsSaveRequestDto,
                                        MultipartFile backgroundImage,
                                        HttpSession session) {


    PoemSettings savedPoemSettings = savedPoem.getPoemSettings();

    setBackgroundImageToPoemSettingsRequestDto(savedPoem, poemSettingsSaveRequestDto, backgroundImage, session, savedPoemSettings);

    //저장된 poem 및 poemSettings에 수정 값 적용
    savedPoem = poemRequestToPoem(poemSaveRequestDto, savedPoem);
    savedPoemSettings = poemSettingsRequestToPoemSettings(poemSettingsSaveRequestDto, savedPoem.getPoemSettings());


    PoemSettings updatedPoemSettings = poemSettingsRepository.save(savedPoemSettings);
    savedPoem.setPoemSettings(updatedPoemSettings);
    poemRepository.save(savedPoem);
  }

  //배경이미지 변경 또는 삭제
  private void setBackgroundImageToPoemSettingsRequestDto(Poem savedPoem, PoemSettingsSaveRequestDto poemSettingsSaveRequestDto, MultipartFile backgroundImage, HttpSession session, PoemSettings savedPoemSettings) {
    try {
      String backgroundImageName = null;
      //새 이미지가 있을 경우
      if (backgroundImage != null && !backgroundImage.isEmpty()) {

        //기존 저장된 이미지가 존재할 경우 기존 이미지파일 삭제
        if (savedPoemSettings.getBackgroundImage() != null && !savedPoemSettings.getBackgroundImage().isEmpty()) {

          String path = "%d/%s".formatted(savedPoem.getPoemId(), savedPoem.getPoemSettings().getBackgroundImage());
          fileUtils.deleteImage(path, ImageType.POEM_BACKGROUND, session);
        }
        backgroundImageName = fileUtils.uploadProfileImage(backgroundImage, savedPoem.getPoemId(), session, ImageType.POEM_BACKGROUND);
        //새 이미지가 없을 경우
      } else {
        //기존 이미지가 존재하고, updateRequestDto에 이미지 이름이 없는경우 -> 기존이미지 삭제
        if ((savedPoemSettings.getBackgroundImage() != null && !savedPoemSettings.getBackgroundImage().isEmpty())
            && poemSettingsSaveRequestDto.getBackgroundImage() == null || poemSettingsSaveRequestDto.getBackgroundImage().isEmpty()) {
          String path = "%d/%s".formatted(savedPoem.getPoemId(), savedPoem.getPoemSettings().getBackgroundImage());
          fileUtils.deleteImage(path, ImageType.POEM_BACKGROUND, session);
        }
      }

      poemSettingsSaveRequestDto.setBackgroundImage(backgroundImageName);

    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  //메인페이지에 표출할 게시글 조회(금월 작성글 중 최다 북마크,최다 조회수 게시글 조회, 금월 작성된 게시글이 없을경우,
  // 전체기간 최다 북마크, 최다 조회수 게시글 조회)
  public Map<String, Object> getPoemsForMainPage() {
    Map<String, Object> poemAndPoemSettings = new HashMap<>();
    Poem poem;

    Optional<Poem> mostViewedPoemThisMonth = poemRepository.selectMostViewedPoemThisMonth();
    if (mostViewedPoemThisMonth.isEmpty()) {
      poem = poemRepository.selectMostViedPoemAllTime()
          .orElseThrow(() -> new ResourceNotFoundException("Any poem written is not found"));
    } else {
      poem = mostViewedPoemThisMonth.get();
    }

    poemAndPoemSettings.put("poem", poemSummaryDtoMapper.apply(poem));
    poemAndPoemSettings.put("poemSettings", poem.getPoemSettings());

    return poemAndPoemSettings;
  }

  //한 회원이 작성한 게시글 조회
  public Page<PoemSummaryDto> getPoemsByMember(Long memberId, PageParameterDto pageParameterDto) {
    int pageNumber = pageParameterDto.getPage() - 1; //Page는 pageNumber 0부터 시작이므로, 전달받은 page에서 -1
    PageRequest pageRequest = PageRequest.of(pageNumber, pageParameterDto.getSize(), Sort.by(Sort.Order.desc(DEFAULT_SORT_FIELD)));

    Page<Poem> poemPages = poemRepository.findByMember_MemberIdAndDeletedIsFalseOrderByWriteDateDesc(memberId, pageRequest);

    return poemPages.map(poemSummaryDtoMapper);
  }

  //카테고리별 게시글 작성 현황 조회
  public List<PoemStatisticsInterface> getPoemStatistics() {
    return poemRepository.selectPoemStatistics();
  }

  //한 회원이 작성한 모든 게시글 삭제
  @Transactional
  public void deleteAllPoemByMember(Member member, HttpSession session) {
    List<Poem> poemsByMember = poemRepository.findByMember(member);

    //회원이 작성한 게시글을 북마크한 내역 삭제
    bookmarkRepository.deleteByPoemIn(poemsByMember);

    //모든 게시글에대해 배경이미지 존재시 삭제
    poemsByMember.stream()
        .map(poems -> poems.getPoemId().toString())
        .forEach(id ->
            fileUtils.deleteImage(id, ImageType.POEM_BACKGROUND, session));

    poemRepository.deleteAllByMember(member);
  }

  //한 회원이 북마크한 게시글 조회
  public Page<PoemSummaryDto> getBookmarkedPoemPage(Long memberId, PageParameterDto pageParameterDto) {
    int pageNumber = pageParameterDto.getPage() - 1; //Page는 pageNumber 0부터 시작이므로, 전달받은 page에서 -1
    PageRequest pageRequest =
        PageRequest.of(pageNumber, pageParameterDto.getSize(), Sort.by(Sort.Order.desc("bookmarkDate")));

    Page<Bookmark> bookmarkPage = bookmarkRepository.findByMember_MemberId(memberId, pageRequest);

    return bookmarkPage.map(bookmark -> poemSummaryDtoMapper.apply(bookmark.getPoem()));
  }


  //저장 또는 수정 정보를 담은 PoemSaveRequestDto를 Poem 으로 변환
  // 전달받은 poem이 있을 경우 해당 poem의 값 수정, 없을 경우 새 Poem 생성
  public Poem poemRequestToPoem(PoemSaveRequestDto poemSaveRequestDto, Poem poem) {

    if (poem == null) {
      return Poem.builder()
          .title(poemSaveRequestDto.getTitle())
          .content(poemSaveRequestDto.getContent())
          .description(poemSaveRequestDto.getDescription())
          .categoryId(poemSaveRequestDto.getCategoryId())
          .member(poemSaveRequestDto.getMember())
          .build();
    } else {
      poem.setTitle(poemSaveRequestDto.getTitle());
      poem.setContent(poemSaveRequestDto.getContent());
      poem.setDescription(poemSaveRequestDto.getDescription());
      poem.setCategoryId(poemSaveRequestDto.getCategoryId());
      return poem;
    }
  }
  //저장 또는 수정 정보를 담은 PoemSettingsSaveRequestDto를 PoemSettings 로 변환
  // 전달받은 PoemSettings가  있을 경우 해당 PoemSettings의 값 수정, 없을 경우 새 PoemSettings 생성
  public PoemSettings poemSettingsRequestToPoemSettings(PoemSettingsSaveRequestDto poemSettingsSaveRequestDto,
                                                        PoemSettings poemSettings) {
    if (poemSettings == null) {
      return PoemSettings.builder()
          .titleFontSize(poemSettingsSaveRequestDto.getTitleFontSize())
          .contentFontSize(poemSettingsSaveRequestDto.getContentFontSize())
          .fontFamily(poemSettingsSaveRequestDto.getFontFamily())
          .color(poemSettingsSaveRequestDto.getColor())
          .textAlign(poemSettingsSaveRequestDto.getTextAlign())
          .backgroundImage(poemSettingsSaveRequestDto.getBackgroundImage())
          .backgroundOpacity(poemSettingsSaveRequestDto.getBackgroundOpacity())
          .build();
    } else {
      poemSettings.setTitleFontSize(poemSettingsSaveRequestDto.getTitleFontSize());
      poemSettings.setContentFontSize(poemSettingsSaveRequestDto.getContentFontSize());
      poemSettings.setFontFamily(poemSettingsSaveRequestDto.getFontFamily());
      poemSettings.setColor(poemSettingsSaveRequestDto.getColor());
      poemSettings.setTextAlign(poemSettingsSaveRequestDto.getTextAlign());
      poemSettings.setBackgroundImage(poemSettingsSaveRequestDto.getBackgroundImage());
      poemSettings.setBackgroundOpacity(poemSettingsSaveRequestDto.getBackgroundOpacity());
      return poemSettings;
    }
  }
}

