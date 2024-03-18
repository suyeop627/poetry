package com.study.poetry.dto.member;

import com.study.poetry.utils.enums.Gender;
import com.study.poetry.utils.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

//회원 가입 요청 dto
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignupRequestDto extends SimpleMemberInfoDto {
  //SimpleMemberInfoDto
  // - String email
  // - String name
  // - String phone

  @Size(min=8, max = 16, message = "password size must be between 8 and 16")
  @NotNull(message = "password must not be null")
  private String password;

  private Set<UserRole> roles = new HashSet<>();

  @NotNull(message = "gender must not be null")
  private Gender gender;

}
