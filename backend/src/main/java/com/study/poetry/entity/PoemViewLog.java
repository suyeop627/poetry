package com.study.poetry.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

//게시글별 조회수 저장 엔티티
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoemViewLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "poem_id")
  @JsonIgnore
  private Poem poem;

  @Column
  private Long memberId;

  @Column(length = 50)
  private String memberIpAddress;
}
