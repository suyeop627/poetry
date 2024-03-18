package com.study.poetry.service;

import com.study.poetry.dto.admin.MemberStatisticsInterface;
import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.member.*;
import com.study.poetry.dto.utils.PageParameterDto;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.Role;
import com.study.poetry.exception.ResourceDuplicatedException;
import com.study.poetry.exception.ResourceNotFoundException;
import com.study.poetry.repository.BookmarkRepository;
import com.study.poetry.repository.MemberRepository;
import com.study.poetry.repository.RefreshTokenRepository;
import com.study.poetry.repository.RoleRepository;
import com.study.poetry.utils.FileUtils;
import com.study.poetry.utils.enums.ImageType;
import com.study.poetry.utils.enums.UserRole;
import com.study.poetry.utils.mapper.MemberDtoMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RoleRepository roleRepository;
  private final PoemService poemService;
  private final BookmarkRepository bookmarkRepository;
  private final MemberDtoMapper memberDtoMapper;
  private final String DEFAULT_SORT_FIELD = "memberId";
  private final RefreshTokenRepository refreshTokenRepository;

  private final FileUtils fileUtils;

  //회원 정보를 db에 저장
  public MemberSignupResponseDto addMember(MemberSignupRequestDto requestDto) {
    checkPhoneDuplicationOrThrow(requestDto.getPhone());

    Member newMember = createMemberFromSignupRequest(requestDto);

    Member savedMember = memberRepository.save(newMember);

    return MemberSignupResponseDto.builder()
        .memberId(savedMember.getMemberId())
        .email(savedMember.getEmail())
        .regdate(savedMember.getRegdate())
        .name(savedMember.getName())
        .build();
  }

  //MemberSignupRequestDto 를 Member 로 변환하여 반환함
  private Member createMemberFromSignupRequest(MemberSignupRequestDto requestDto) {
    Member member = Member.builder()
        .email(requestDto.getEmail())
        .name(requestDto.getName())
        .phone(requestDto.getPhone())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .gender(requestDto.getGender())
        .build();

    addRoleToNewMember(requestDto, member); //call by reference

    return member;
  }

  //MemberSignupRequestDto에 Role이 없을경우, ROLE_USER를 추가하고, Role이 포함된 경우, Set<Role>의 형식으로 변환하여 Member에 추가.
  private void addRoleToNewMember(MemberSignupRequestDto requestDto, Member newMember) {
    //일반회원 권한 추가
    if (requestDto.getRoles().size() == 0) {
      newMember.addRole(getRolesOrThrow(UserRole.ROLE_USER));
    } else {
      Set<Role> roles = getRequestedRolesOrThrow(requestDto.getRoles());
      newMember.setRoles(roles);
    }
  }

  //UserRole에 해당하는 Role을 Db에서 조회하여 반환
  private Role getRolesOrThrow(UserRole userRole) {
    return roleRepository.findByName(userRole)
        .orElseThrow(() -> new ResourceNotFoundException("%s is not exists".formatted(userRole.name())));
  }

  //UserRole이 다수의 값을 가진 경우, DB에 존재하는지 확인 후, Set<Role>로 변환하여 반환.
  private Set<Role> getRequestedRolesOrThrow(Set<UserRole> userRoles) {
    List<Role> allRoles = roleRepository.findAll();

    Set<UserRole> allUserRole =
        allRoles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

    Set<UserRole> nonexistentRoles =
        userRoles.stream()
            .filter(userRole -> !allUserRole.contains(userRole))
            .collect(Collectors.toSet());

    //존재하지 않는 role이 있다면, 예외 발생
    if (nonexistentRoles.size() != 0) {
      throw new ResourceNotFoundException("Request contains nonexistent role. %s".formatted(nonexistentRoles));
    }
    //모두 존재한다면 request의 UserRole과 일치하는 Set<Role> 반환
    return allRoles.stream()
        .filter(role -> userRoles.contains(role.getName()))
        .collect(Collectors.toSet());
  }

  //전화번호의 중복여부 판단
  private void checkPhoneDuplicationOrThrow(String phone) {
    if (memberRepository.existsByPhone(phone)) {
      throw new ResourceDuplicatedException("phone number entered is duplicated");
    }
  }

  //회원 정보 수정
  public Member updateMember(Long memberId,
                             MemberUpdateRequestDto updateRequestDto,
                             MultipartFile profileImage,
                             LoginMemberInfo loginMemberInfo,
                             HttpSession session) throws BadRequestException {

    Member member = getMemberByIdOrThrow(memberId);

    //수정하려는 대상 회원 id와 로그인한 회원의 id가 다를 경우 비밀번호 일치여부 확인
    //controller에서 관리자인지 로그인한 본인 회원수정인지 확인하였으므로,
    //대상 회원 id와 로그인한회원 id가 다를 경우, 관리자가 타 회원 정보를 수정하는 상황->비밀번호 확인 불필요
    //대상회원 id와 로그인한 회원 id가 같을 경우, 본인 정보를 수정하는 상황 -> 비밀번호 확인 필요
    if (loginMemberInfo.getMemberId().equals(memberId)) {
      checkMemberPasswordOrThrow(member, updateRequestDto.getPassword());
    }
    //전화번호 변경 시 중복 여부 확인
    if (!member.getPhone().equals(updateRequestDto.getPhone())) {
      checkPhoneDuplicationOrThrow(updateRequestDto.getPhone());
    }
    setProfileImageName(updateRequestDto, profileImage, session, member);
    member.setPhone(updateRequestDto.getPhone());
    member.setName(updateRequestDto.getName());
    member.setGender(updateRequestDto.getGender());

    return memberRepository.save(member);
  }

  //사용자 프로필 기존 프로필 이미지 삭제 또는 재설정
  private void setProfileImageName(MemberUpdateRequestDto updateRequestDto, MultipartFile profileImage, HttpSession session, Member member) {
    try {
      String profileImageName = null;
      //새 이미지가 있을 경우
      if (profileImage != null && !profileImage.isEmpty()) {
        //기존 저장된 이미지가 존재할 경우 기존 이미지파일 삭제
        if (member.getProfileImage() != null && !member.getProfileImage().isEmpty()) {
          String path = String.format("%d/%s", member.getMemberId(), member.getProfileImage());
          fileUtils.deleteImage(path, ImageType.PROFILE, session);
        }

         profileImageName = fileUtils.uploadProfileImage(profileImage, member.getMemberId(), session, ImageType.PROFILE);
      //새이미지가 없는 경우
      }else{
        //기존 이미지가 존재하고, updateRequestDto에 이미지 이름이 없는경우 -> 기존이미지 삭제
        if((updateRequestDto.getProfileImage() == null || updateRequestDto.getProfileImage().isEmpty())
        && (member.getProfileImage() != null && !member.getProfileImage().isEmpty())){
          String path = String.format("%d/%s", member.getMemberId(), member.getProfileImage());
          fileUtils.deleteImage(path, ImageType.PROFILE, session);
        }
      }
    member.setProfileImage(profileImageName);

    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  //memberId에 해당하는 회원 조회
  public Member getMemberByIdOrThrow(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format("member id %s is not found", memberId)));
  }

  //memberId에 해당하는 Member를 MemberDto로 변환하여 반환
  public MemberDto getMemberDto(Long memberId) {
    return memberDtoMapper.apply(getMemberByIdOrThrow(memberId));
  }

  //조건에 따라 조회된 Page<Member>를 Page<MemberDto>로 변환하여 반환
  public Page<MemberDto> getMemberList(PageParameterDto pageParameterDto) {
    int pageNumber = pageParameterDto.getPage() - 1; //Page는 pageNumber 0부터 시작이므로, 전달받은 page에서 -1
    PageRequest pageRequest = PageRequest.of(pageNumber, pageParameterDto.getSize(), Sort.by(Sort.Order.desc(DEFAULT_SORT_FIELD)));
    Page<Member> memberPages;
    switch (pageParameterDto.getType()) {
      case "name" -> memberPages = memberRepository
          .findByNameContainingIgnoreCaseOrderByRegdateDesc(pageParameterDto.getKeyword(), pageRequest);
      case "email" -> memberPages = memberRepository
          .findByEmailContainingIgnoreCaseOrderByRegdateDesc(pageParameterDto.getKeyword(), pageRequest);
      case "phone" -> memberPages = memberRepository
          .findByPhoneContainingIgnoreCaseOrderByRegdateDesc(pageParameterDto.getKeyword(), pageRequest);
      case "role" -> memberPages = memberRepository
          .findByRoles_NameOrderByRegdateDesc(UserRole.valueOf(pageParameterDto.getKeyword()), pageRequest);
      default -> memberPages = memberRepository.findAll(pageRequest);
    }
    return memberPages.map(memberDtoMapper);
  }

  //memberId에 해당하는 회원 정보(해당 회원이 북마크한내역, 작성한 시, 프로필 이미지) 삭제
  @Transactional
  public void deleteMember(Long memberId, HttpSession session) {
    invalidateToken(memberId);
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() ->
            new ResourceNotFoundException(String.format("member id : %s, is nonexistent", memberId)));

    //회원이 북마크한 내역 삭제
    bookmarkRepository.deleteByMember(member);
    //회원이 작성한 게시글 삭제
    poemService.deleteAllPoemByMember(member, session);

    if (member.getProfileImage() != null && !member.getProfileImage().isEmpty()) {
      String path = member.getMemberId().toString();
      fileUtils.deleteImage(path, ImageType.PROFILE, session);
    }
    memberRepository.deleteById(memberId);
  }

  //토큰 무효화 처리
  private void invalidateToken(Long memberId) {
    refreshTokenRepository.deleteByMemberId(memberId);
  }

  //이메일이 존재하는지 확인
  public void isExistEmailOrThrow(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new ResourceDuplicatedException("Email is duplicated.");
    }
  }

  //이메일이 존재하지 않는지 확인
  public void isNonExistentEmailOrThrow(String email) {
    memberRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Email doesn't exist."));
  }

  //회원 정보 수정에서 비밀번호 변경 요청시 비밀번호 변경 처리
  public void changePasswordForMemberUpdate(Long memberId, PasswordChangeDto passwordChangeDto) throws BadRequestException {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() ->
            new ResourceNotFoundException(String.format("member id : %s, is nonexistent", memberId)));

    checkMemberPasswordOrThrow(member, passwordChangeDto.getPassword());

    String newPasswordEncoded = passwordEncoder.encode(passwordChangeDto.getNewPassword());
    member.setPassword(newPasswordEncoded);
    memberRepository.save(member);
  }

  //비밀번호 일치여부 확인
  private void checkMemberPasswordOrThrow(Member member, String password) throws BadRequestException {
    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new BadRequestException("Inserted password not matches with member's password");
    }
  }

  //회원 프로필 이미지 반환
  public ResponseEntity<?> getProfileImageResponse(Long memberId, String profileImageName, ImageType imageType, HttpSession session, HttpServletResponse response) throws IOException {
    return fileUtils.getImageResponse(memberId, profileImageName, imageType, session, response);
  }



  //비밀번호 분실시, 비밀번호 변경 요청 처리
  public void changeForgottenPassword(NewPasswordRequestDto newPasswordRequestDto) {
    Member member = memberRepository.findByEmail(newPasswordRequestDto.getEmail())
        .orElseThrow(() ->
            new ResourceNotFoundException("member(email: %s)is not found".formatted(newPasswordRequestDto.getEmail())));

    member.setPassword(passwordEncoder.encode(newPasswordRequestDto.getNewPassword()));
    memberRepository.save(member);
  }

  //관리자 또는 요청 pathVariable 의 id가 회원 본인인지 확인
  public void isAdminOrMemberOwnOrThrow(Long uriMemberId, LoginMemberInfo loginMember) {
    if (!loginMember.getRoles().contains(UserRole.ROLE_ADMIN.name())
        && !uriMemberId.equals(loginMember.getMemberId())) {
      throw new AccessDeniedException(
          String.format("Member (memberId: %s, roles: %s) does not have permission to change data.",
              loginMember.getMemberId(), loginMember.getRoles())
      );
    }
  }

  //회원 현황 정보 반환
  public MemberStatisticsInterface getMemberStatistics() {
    return memberRepository.selectMemberStatistics();
  }

  //관리자 목록 반환
  public List<MemberDto> getAdminList() {
    return memberRepository
        .findByRoles_NameOrderByMemberId(UserRole.ROLE_ADMIN)
        .stream()
        .map(memberDtoMapper)
        .toList();
  }
}


