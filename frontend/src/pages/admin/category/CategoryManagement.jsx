import * as React from "react";
import { Box, Typography, TextField, Button, IconButton } from "@mui/material";
import { useState } from "react";
import { useEffect } from "react";
import { ArrowDownward, ArrowUpward } from "@mui/icons-material";
import jwtAxios from "../../../auth/jwtAxios";

//관리자 카테고리 관리 페이지
export default function CategoryManagement({ categories, setCategories }) {
  const [categoryList, setCategoryList] = useState([]);

  //카테고리 state 저장
  useEffect(() => {
    setCategoryList(categories);
  }, [categories]);

  //카테고리 이름 변경
  const handleCategoryNameChange = (categoryId, newName) => {
    const updatedCategories = categoryList.map((category) => {
      if (category.categoryId === categoryId) {
        return { ...category, categoryName: newName };
      }
      return category;
    });
    setCategoryList(updatedCategories);
  };

  //↑ 버튼 클릭 시,카테고리 순서 앞으로 변경
  const handleMoveUp = (categoryId) => {
    const index = categoryList.findIndex((category) => category.categoryId === categoryId);
    if (index > 0) {
      const updatedCategories = [...categoryList];
      const temp = updatedCategories[index];
      updatedCategories[index] = updatedCategories[index - 1];
      updatedCategories[index - 1] = temp;
      const categoriesToSave = changeOrder(updatedCategories);
      setCategoryList(categoriesToSave);
    }
  };
  //카테고리 순서 (변경된 위치 순서대로 순서 새로 입력)
  const changeOrder = (updatedCategories) => {
    updatedCategories.map((category, index) => (category.categoryOrder = index + 1));
    return updatedCategories;
  };

  //↓ 버튼 클릭 시,카테고리 순서 뒤로 변경
  const handleMoveDown = (categoryId) => {
    const index = categoryList.findIndex((category) => category.categoryId === categoryId);
    if (index < categoryList.length - 1) {
      const updatedCategories = [...categoryList];
      const temp = updatedCategories[index];
      updatedCategories[index] = updatedCategories[index + 1];
      updatedCategories[index + 1] = temp;
      const categoriesToSave = changeOrder(updatedCategories);
      setCategoryList(categoriesToSave);
    }
  };

  //수정사항 저장 요청
  const handleSaveChanges = async () => {
    console.log(categoryList);
    const invalidCategories = categoryList.filter(
      (category) => category.categoryName.length === 0 || category.categoryName.length > 10 || category.categoryName.includes(" ")
    );
    if (invalidCategories.length > 0) {
      alert("카테고리 이름은 1자 이상, 10자 이하이고, 공백을 포함할 수 없습니다. 합니다.");
    } else {
      try {
        const response = await jwtAxios.put("http://localhost:8080/admin/category", categoryList);
        if (response.status === 200) {
          setCategories(response.data);
          alert("변경내역이 저장됐습니다.");
        }
      } catch (error) {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };
  return (
    <Box sx={{ mt: 10, display: "flex", flexDirection: "column", alignItems: "center" }}>
      <Typography variant="h4" sx={{ mb: 5 }}>
        카테고리 관리
      </Typography>
      {categoryList.map((category, index) => (
        <Box key={category.categoryId} sx={{ display: "flex", alignItems: "center", marginBottom: 1 }}>
          <Typography>{index + 1}</Typography>
          <TextField
            value={category.categoryName}
            onChange={(e) => handleCategoryNameChange(category.categoryId, e.target.value)}
            sx={{ marginLeft: 1, marginRight: 1 }}
            inputProps={{ maxLength: 10 }}
          />
          <IconButton onClick={() => handleMoveUp(category.categoryId)}>
            <ArrowUpward />
          </IconButton>
          <IconButton onClick={() => handleMoveDown(category.categoryId)}>
            <ArrowDownward />
          </IconButton>
        </Box>
      ))}
      <Box sx={{ display: "flex", mt: 10 }}>
        <Button variant="contained" onClick={handleSaveChanges} sx={{ ml: 2 }}>
          변경 사항 저장
        </Button>
      </Box>
    </Box>
  );
}
