package com.study.poetry.security;

import com.study.poetry.entity.Member;
import com.study.poetry.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
//DaoAuthenticationProvider 에 의해서 UsernamePasswordToken 을 인증할 때 사용됨
//입력받은 username으로,  db에 저장된 member를 조회함.
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<Member> member = memberRepository.findByEmail(email);
    return new MemberDetails(member.orElseThrow(()->
        new UsernameNotFoundException(String.format("Member email : %s is not found.", email))));
  }
}
