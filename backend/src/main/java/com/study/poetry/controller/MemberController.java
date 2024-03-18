package com.study.poetry.controller;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.auth.LoginRequestDto;
import com.study.poetry.dto.error.ErrorDto;
import com.study.poetry.dto.member.*;
import com.study.poetry.dto.utils.PageParameterDto;
import com.study.poetry.entity.Member;
import com.study.poetry.service.AuthenticationService;
import com.study.poetry.service.EmailService;
import com.study.poetry.service.MemberService;
import com.study.poetry.service.ReportService;
import com.study.poetry.utils.WebUtils;
import com.study.poetry.utils.annotation.TokenToMemberInfo;
import com.study.poetry.utils.enums.ImageType;
import com.study.poetry.utils.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
@RequestMapping("members")
@Slf4j
//회원 관련 요청 처리 담당
public class MemberController {
  private final MemberService memberService;
  private final ReportService reportService;
  private final AuthenticationService authenticationService;
  private final EmailService emailService;

  //회원가입 또는 비밀번호 찾기에서 사용될 이메일 인증코드 발송 처리
  @PostMapping("/email/{usage}")
  public ResponseEntity<?> email(@RequestBody String email, @PathVariable("usage") String usage) {

    log.info("Email Verification for {}", email);
    switch (usage) {
      case "signIn" -> {
        ResponseEntity<BannedAccountResponseDto> responseForbidden = authenticationService.responseForbiddenIfBannedAccount(email);
        if (responseForbidden != null) return responseForbidden;
        memberService.isExistEmailOrThrow(email);
      }
      case "findPassword" -> memberService.isNonExistentEmailOrThrow(email);
    }
    try {
      emailService.sendEmailVerificationMail(email);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  //사용자가 입력한 인증코드와 서버에서 발송한 이메일 인증코드의 일치여부 확인 요청 처리
  @PostMapping("/verifyCode")
  public ResponseEntity<?> verifyCode(@Valid @RequestBody EmailVerificationRequestDto emailVerificationRequestDto,
                                      BindingResult bindingResult,
                                      HttpServletRequest request) {
    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    log.info("Email: {} is attempting for verifying code with '{}'",
        emailVerificationRequestDto.getEmail(), emailVerificationRequestDto.getVerifyCode());

    if (emailService.isValidVerifyCode(emailVerificationRequestDto)) {
      return new ResponseEntity<>(HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  //회원 가입 요청 처리
  @PostMapping
  public ResponseEntity<?> signup(@Valid @RequestBody MemberSignupRequestDto signupRequestDto,
                                  BindingResult bindingResult,
                                  HttpServletRequest request) {
    log.info("Attempting signup for user with email: {}, name: {}",
        signupRequestDto.getEmail(), signupRequestDto.getName());

    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    MemberSignupResponseDto memberSignupResponseDto = memberService.addMember(signupRequestDto);
    URI uri = WebUtils.getCreatedUri(memberSignupResponseDto.getMemberId());

    log.info("Created user uri: {}", uri);
    return ResponseEntity.created(uri).body(memberSignupResponseDto);
  }

  //회원 목록 조회 요청 처리
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<MemberDto>> getMemberList(@ModelAttribute PageParameterDto pageParameterDto) {
    log.info("Method: getAllMembers called with page: {}, size: {}",
        pageParameterDto.getPage(), pageParameterDto.getSize());
    Page<MemberDto> memberDtoPage = memberService.getMemberList(pageParameterDto);

    log.info("Retrieved {} members in page {}. Total pages: {}, Total members: {}.",
        memberDtoPage.getNumberOfElements(), pageParameterDto.getPage(),
        memberDtoPage.getTotalPages(), memberDtoPage.getTotalElements());

    return ResponseEntity.ok().body(memberDtoPage);
  }

  //단일 회원 정보 조회
  @GetMapping("/{memberId}")
  public ResponseEntity<?> getMember(@PathVariable("memberId") Long memberId) {
    log.info("Method: getMember called with memberId {}", memberId);
    return ResponseEntity.ok(memberService.getMemberDto(memberId));
  }

  //프로필 이미지 조회 요청 처리
  @GetMapping("/{memberId}/profileImage/{profileImageName}")
  public ResponseEntity<?> getProfileImage(@PathVariable("memberId") Long memberId,
                                           @PathVariable("profileImageName") String profileImageName,
                                           HttpSession session,
                                           HttpServletResponse response) throws IOException {
    return memberService.getProfileImageResponse(memberId, profileImageName, ImageType.PROFILE, session, response);
  }

  //회원 정보 수정
  //ROLE_ADMIN : 모든 회원의 정보 수정 가능
  //ROLE_USER : 전달받은 memberId와 access token에 담긴 memberId가 일치하는 경우에만 수정 가능
  @PutMapping("/{memberId}")
  public ResponseEntity<?> updateMember(@Valid @RequestPart(value = "updateInfo") MemberUpdateRequestDto updateRequestDto, //최상단으로
                                        BindingResult bindingResult,
                                        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                                        @PathVariable("memberId") Long memberId,
                                        @TokenToMemberInfo LoginMemberInfo loginMember,
                                        HttpServletRequest request,
                                        HttpSession session) throws BadRequestException {

    log.info("Attempting to update target member(memberId: {}) by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());

    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    memberService.isAdminOrMemberOwnOrThrow(memberId, loginMember);

    Member updatedMember = memberService.updateMember(memberId, updateRequestDto, profileImage, loginMember, session);

    log.info("Update request successful. Target member(memberId: {}) info updated by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());

    //사용자 본인의 정보를 수정한 경우, 재 로그인처리로 새로운 토큰을 포함한 응답
    if (memberId.equals(loginMember.getMemberId())) {
      LoginRequestDto updatedMemberLoginRequest =
          new LoginRequestDto(updatedMember.getEmail(), updateRequestDto.getPassword());

      return ResponseEntity.ok().body(authenticationService.login(updatedMemberLoginRequest));
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  //비밀번호 변경 요청 처리(회원 정보 수정)
  @PutMapping("/{memberId}/password")
  public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordChangeDto passwordChangeDto,
                                          BindingResult bindingResult,
                                          @PathVariable("memberId") Long memberId,
                                          @TokenToMemberInfo LoginMemberInfo loginMember,
                                          HttpServletRequest request) throws BadRequestException {

    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;

    log.info("Attempting to update target member password (memberId: {}) by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());

    memberService.isAdminOrMemberOwnOrThrow(memberId, loginMember);

    log.info("Change Password request successful. Target member(memberId: {}) info updated by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());
    memberService.changePasswordForMemberUpdate(memberId, passwordChangeDto);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  //비밀번호 변경 요청 처리(비밀번호 분실)
// 비밀번호 분실 시, 입력된 회원정보를 기준으로 비밀번호 변경 권한 확인 불가능-> email 인증이후, 비밀번호 변경하도록 처리)
  @PutMapping("/password")
  public ResponseEntity<?> updateForgottenPassword(@Valid @RequestBody NewPasswordRequestDto newPasswordRequestDto,
                                                   BindingResult bindingResult,
                                                   HttpServletRequest request) {
    ResponseEntity<Set<ErrorDto>> errorDtoSet = WebUtils.getErrorResponseFromBindingResult(bindingResult, request);
    if (errorDtoSet != null) return errorDtoSet;
    log.info("{} is attempting for change password.", newPasswordRequestDto.getEmail());
    memberService.changeForgottenPassword(newPasswordRequestDto);
    return ResponseEntity.ok().build();
  }


  //ROLE_ADMIN이거나, 로그인한 회원의 Id와 수정할 회원의 Id가 일치하는지 판단


  //회원 삭제 요청 처리
  //ROLE_ADMIN : 모든 회원의 정보 삭제 가능
  //ROLE_USER : 전달받은 memberId와 access token에 담긴 memberId가 일치하는 경우에만 삭제 가능
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @DeleteMapping("/{memberId}")
  public ResponseEntity<?> deleteMember(@PathVariable("memberId") Long memberId,
                                        @TokenToMemberInfo LoginMemberInfo loginMember,
                                        HttpSession session) {
    log.info("member(memberId: {}) deletion request from memberId: {}, role: {}", memberId,
        loginMember.getMemberId(), loginMember.getRoles());

    memberService.isAdminOrMemberOwnOrThrow(memberId, loginMember);

    //회원이 작성한 게시글이 신고된거 삭제, 회원이 신고했던 내역 삭제
    deleteRegardingReportByMember(memberId);

    //회원이 북마크한거 삭제, 회원이 작성한 글 삭제, 배경이미지 삭제, 회원삭제
    memberService.deleteMember(memberId, session);

    log.info("Deletion request successful. Target member(memberId: {}) info deleted by member(memberId: {}, roles: {})",
        memberId, loginMember.getMemberId(), loginMember.getRoles());
    return ResponseEntity.ok().build();
  }

  //서비스 이용 제한 회원의 탈퇴 요청 처리
  //서비스 제한된 회원의 경우, 회원정보를 기준으로 권한 확인 불가능.
  // -> 첫 로그인 시도시 회원 id 전달, 이후 탈퇴 요청시 해당 id를 전달받아 탈퇴하도록 처리
  @DeleteMapping("{memberId}/restricted")
  public ResponseEntity<?> deleteRestrictedMember(@PathVariable Long memberId,
                                                  HttpSession session) {
    log.info("Restricted member(id: {}) requests deletion account.", memberId);

    deleteRegardingReportByMember(memberId);

    memberService.deleteMember(memberId, session);

    log.info("Deletion request for restricted member successful. Target member(memberId: {})", memberId);
    return ResponseEntity.ok().build();
  }


  //삭제하려는 회원과 관련된 신고 관련 데이터 삭제
  private void deleteRegardingReportByMember(Long memberId) {
    log.info("Delete report and report details regarding member {}", memberId);
    Member member = memberService.getMemberByIdOrThrow(memberId);

    reportService.deleteAllRegardingReportByMember(member);

    //삭제하려는 회원이 관리자이며, 최종 수정한 신고 내역이 존재할 경우, 최종 수정자를 null로 변경
    if (member.getRoleNameSet().contains(UserRole.ROLE_ADMIN.name())) {
      log.info("Member id({}) is admin account. Find report done by member{} and set to null ", memberId, memberId);
      reportService.updateReportDoneByMemberToNull(memberId);
    }
  }
}
