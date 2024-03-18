package com.study.poetry.service;

import com.study.poetry.entity.Category;
import com.study.poetry.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
  private final  CategoryRepository categoryRepository;
  private final String CATEGORY_SORT_PROPERTY = "categoryOrder";
  //카테고리 목록 조회
  public List<Category> selectCategories(){
    Sort categorySort = Sort.by(Sort.Order.asc(CATEGORY_SORT_PROPERTY));
    return categoryRepository.findAll(categorySort);
  }
}
