package com.study.poetry.security;

import com.study.poetry.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;
//UserDetailsService 에 의해 찾아질 UserDetails의 구현체
public class MemberDetails implements UserDetails {
  private final Member member;
  public MemberDetails(Member member) {
    this.member = member;
  }
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return member.getRoles()
        .stream()
        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
        .collect(Collectors.toSet());
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public String getUsername() {
    return member.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public Member getMember(){
    return member;
  }
}
