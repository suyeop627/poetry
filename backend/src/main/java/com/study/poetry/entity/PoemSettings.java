package com.study.poetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//게시글 설정 정보 저장 엔티티
@Entity
@Table(name = "poem_settings")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoemSettings {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long poemSettingsId;

  @Column(length = 50)
  private String titleFontSize;

  @Column(length = 50)
  private String contentFontSize;

  @Column(length = 50)
  private String fontFamily;

  @Column(length = 50)
  private String color;

  @Column(length = 50)
  private String textAlign;

  @Column(length = 500)
  private String backgroundImage;

  @Column
  private Float backgroundOpacity;


}
