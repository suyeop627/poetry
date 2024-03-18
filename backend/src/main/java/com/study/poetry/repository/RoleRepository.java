package com.study.poetry.repository;

import com.study.poetry.entity.Role;
import com.study.poetry.utils.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(UserRole name);
}
