package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
//북마크 정보 저장 엔티티
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Bookmark {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bookmark_id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "poem_id", nullable = false)
  private Poem poem;

  @CreationTimestamp
  private LocalDateTime bookmarkDate;
}
