import React, { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import PoemCard from "./PoemCard";
import { Box, Button, Container, Typography } from "@mui/material";
import BasicPagination from "../../components/BasicPagination";
import axios from "axios";

//회원별 작성한 게시글 및 북마크한 게시글 목록 표출
const MemberPoemList = ({ categories }) => {
  const { memberId, name, tabNumber } = useParams();

  const [poemList, setPoemList] = useState({});
  const { state } = useLocation();
  //tab=0 :회원이 작성한 글, tab=1 회원이 북마크한 글
  const [tab, setTab] = useState(parseInt(tabNumber));

  //상세페이지에서 '뒤로가기' 버튼으로 접근한경우 state에 이전 목록 설정 값을 전달받아 현재 목록에 적용
  const [pageParameter, setPageParameter] = useState({
    page: state ? state.pageParameter.page : 1,
    size: state ? state.pageParameter.size : 10,
    totalPages: state ? state.pageParameter.totalPages : 1,
  });

  //페이지, tab, 회원 id변경에 따라 글 목록 또는 북마크 목록 조회
  useEffect(() => {
    if (tab === 0) {
      getPoemPage(pageParameter);
    } else {
      getBookmarkPage(pageParameter);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageParameter.page, memberId, tab]);

  //회원이 작성한 글 목록 조회
  const getPoemPage = async (pageParameter) => {
    try {
      const { page, size } = pageParameter;
      const response = await axios.get(`http://localhost:8080/poems/members/${memberId}`, {
        params: {
          page: page,
          size: size,
        },
      });
      if (response.status === 200) {
        if (response.data.totalPages > 0) {
          setPoemList(response.data.content);
          setPageParameter({ ...pageParameter, totalPages: response.data.totalPages, page: response.data.number + 1 });
        } else {
          setPoemList([]);
          setPageParameter({ ...pageParameter, page: 1, totalPages: 1 });
        }
      }
    } catch (error) {
      console.log(error);
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  //회원이 북마크한 글목록 조회
  const getBookmarkPage = async (pageParameter) => {
    try {
      const { page, size } = pageParameter;
      const response = await axios.get(`http://localhost:8080/poems/bookmark/${memberId}`, {
        params: {
          page: page,
          size: size,
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
  //검색조건 변경사항 반영
  const handleChange = (key, value) => {
    setPageParameter({
      ...pageParameter,
      [key]: value,
    });
  };
  //탭 변경사항 반영
  const handleTabChange = (tabNum) => {
    const resetPageParamter = { page: 1, size: 10, totalPages: 1 };
    setPageParameter(resetPageParamter);
    setTab(tabNum);
  };

  return (
    <>
      <Container sx={{ mt: 10 }}>
        <Box sx={{ width: 450, mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 1 }}>
            {name}님의 공간
          </Typography>
          <Button variant={tab === 0 ? "contained" : "outlined"} onClick={() => handleTabChange(0)}>
            작성한 시
          </Button>

          <Button variant={tab === 1 ? "contained" : "outlined"} onClick={() => handleTabChange(1)}>
            담은 시
          </Button>
        </Box>
        <Box>
          {poemList.length ? (
            poemList.map((poem) => (
              <PoemCard
                key={poem.poemId}
                poem={poem}
                pageParameter={pageParameter}
                fromUrl={`/poems/members/${memberId}/${name}/${tab}`}
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

export default MemberPoemList;
