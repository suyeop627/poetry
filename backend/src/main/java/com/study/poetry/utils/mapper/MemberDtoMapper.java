package com.study.poetry.utils.mapper;

import com.study.poetry.entity.Member;
import com.study.poetry.dto.member.MemberDto;
import org.springframework.stereotype.Component;

import java.util.function.Function;
//db에서 조회된 Member 중 클라이언트로 전달할 정보만 담긴 MemberDto로 변환
@Component
public class MemberDtoMapper implements Function<Member, MemberDto> {

  @Override
  public MemberDto apply(Member member) {
    return MemberDto.builder()
        .memberId(member.getMemberId())
        .roles(member.getRoleNameSet())
        .email(member.getEmail())
        .name(member.getName())
        .gender(member.getGender())
        .phone(member.getPhone())
        .profileImage(member.getProfileImage())
        .build();
  }
}
