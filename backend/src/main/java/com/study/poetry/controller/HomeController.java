package com.study.poetry.controller;

import com.study.poetry.service.HomeService;
import com.study.poetry.service.PoemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
//메인페이지 표출 데이터 관련 요청 담당
public class HomeController {
  private final HomeService homeService;
  private final PoemService poemService;
  //메인 페이지에 표출할 게시글 조회
  @GetMapping("mainContent")
  public ResponseEntity<?> getMainContent(){
    return ResponseEntity.ok(poemService.getPoemsForMainPage());
  }

  //카테고리 조회
  @GetMapping("category")
  public ResponseEntity<?> getCategories() {
    return ResponseEntity.ok(homeService.selectCategories());
  }
}
