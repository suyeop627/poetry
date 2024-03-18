package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//게시글 정보저장 엔티티
@Entity
@Table(name = "poem")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class Poem {
  @Id
  @Column(name = "poem_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long poemId;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column
  private Integer categoryId;

  @Column(length = 100)
  private String title;

  @Column(length = 1000)
  private String content;

  @Column(length = 100)
  private String description;

  @CreationTimestamp
  private LocalDateTime writeDate;


  @OneToMany(mappedBy = "poem", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  private List<PoemViewLog> viewLogs = new ArrayList<>();

  @OneToOne(cascade = {CascadeType.REMOVE})
  @JoinColumn(name = "poem_settings_id")
  private PoemSettings poemSettings;

  @Column
  @Builder.Default
  private boolean deleted =false;
}
