import React, { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import PoemCard from "./PoemCard";
import { Box, Container, Typography } from "@mui/material";
import SearchComponent from "../../../components/common/SearchComponent";
import BasicPagination from "../../components/BasicPagination";
import axios from "axios";

//카테고리별 게시글 목록 페이지
const PoemList = ({ categories }) => {
  const { categoryId } = useParams();
  const [poemList, setPoemList] = useState({});
  const [nowCategory, setNowCategory] = useState({
    categoryId: "",
    categoryName: "",
    categoryOrder: "",
  });
  const { state } = useLocation();
  //상세페이지에서 '뒤로가기' 버튼으로 접근한경우 state에 이전 목록 설정 값을 전달받아 현재 목록에 적용
  const [pageParameter, setPageParameter] = useState({
    categoryId: state?.pageParameter ? state.pageParameter.categoryId : "",
    page: state?.pageParameter ? state.pageParameter.page : 1,
    size: state?.pageParameter ? state.pageParameter.size : 10,
    type: state?.pageParameter ? state.pageParameter.type : "title",
    keyword: state?.pageParameter ? state.pageParameter.keyword : "",
    totalPages: state?.pageParameter ? state.pageParameter.totalPages : 1,
  });

  const navigate = useNavigate();

  //선택한 카테고리 변경에따라 해당 글 목록 조회요청
  useEffect(() => {
    if (categoryId && categories.length > 0 && !state) {
      const selectedCategory = categories.find((category) => category.categoryId.toString() === categoryId);
      if (!selectedCategory) {
        alert("잘못된 요청입니다.");
        navigate(-1);
      }

      setNowCategory(selectedCategory);
      const pageParameterObj = { ...pageParameter, categoryId: selectedCategory.categoryId, page: 1 };
      getPoemPage(pageParameterObj);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [categories, categoryId]);

  useEffect(() => {
    getPoemPage(pageParameter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageParameter.page]);

  //게시글 목록 조회 요청
  const getPoemPage = async (pageParameter) => {
    try {
      const { categoryId, page, size, type, keyword } = pageParameter;
      const response = await axios.get(`http://localhost:8080/poems`, {
        params: {
          categoryId: categoryId,
          page: page,
          size: size,
          type: type,
          keyword: keyword,
        },
      });
      if (response.status === 200) {
        if (response.data.totalPages > 0) {
          setPoemList(response.data.content);
          setPageParameter({ ...pageParameter, totalPages: response.data.totalPages });
        } else {
          setPoemList([]);
          setPageParameter({ ...pageParameter, totalPages: 1 });
        }
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //검색조건 변경 값 반영
  const handleChange = (key, value) => {
    setPageParameter({
      ...pageParameter,
      [key]: value,
    });
  };

  return (
    <>
      <Container sx={{ mt: 10 }}>
        <Box sx={{ width: 500, mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 1 }}>
            {nowCategory?.categoryName ? `'${nowCategory.categoryName}' 에 대한 시` : ""}
          </Typography>
          <SearchComponent handleChange={handleChange} pageParameter={pageParameter} doSearch={getPoemPage} target={"poem"} />
        </Box>
        <Box>
          {poemList.length ? (
            poemList.map((poem) => (
              <PoemCard
                key={poem.poemId}
                poem={poem}
                pageParameter={pageParameter}
                fromUrl={`/poems/category/${categoryId}`}
                categories={categories}
              />
            ))
          ) : (
            <Typography variant="h6">게시글이 없습니다.</Typography>
          )}
        </Box>
        <BasicPagination pageParameter={pageParameter} page={pageParameter.page} handleChange={handleChange} />
      </Container>
    </>
  );
};

export default PoemList;
