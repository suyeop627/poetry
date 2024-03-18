package com.study.poetry.dto.member;

import com.study.poetry.utils.enums.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

//회원 정보 수정 요청 dto
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//equals및 hashCode 호출 시에 부모클래스의 필드와 자손클래스에서 추가된 필드를 모두 비교.  callSuper = false 로 하면 부모클래스의 필드는 무시한 채, 자손클래스의 필드만 비교
public class MemberUpdateRequestDto extends SimpleMemberInfoDto {
  //SimpleMemberInfoDto
  // - String email
  // - String name
  // - String phone

  private String password;

  @NotNull(message = "gender must not be null")
  private Gender gender;

  private String profileImage;

  @NotNull(message = "roles must not be null")
  private Set<String> roles;

}
