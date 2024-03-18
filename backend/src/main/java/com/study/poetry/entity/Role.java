package com.study.poetry.entity;

import com.study.poetry.utils.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//회원 역할 저장 엔티티
@Entity
@Table(name="role")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Role {
  @Id
  @Column(name="role_id")
  private Long roleId;

  @Column
  @Enumerated(EnumType.STRING)
  private UserRole name;
}