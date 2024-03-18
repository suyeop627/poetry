package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//카테고리 저장 엔티티
@Entity
@Table(name="category")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer categoryId;

  @Column(length = 20)
  private String categoryName;

  @Column
  private Long categoryOrder;
}
