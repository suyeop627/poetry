import * as React from "react";
import { Avatar, Box, Card, CardContent, Container, Drawer, Grid, Typography } from "@mui/material";
import jwtAxios from "../../../auth/jwtAxios";
import { useState } from "react";
import BasicPagination from "../../components/BasicPagination";
import { useEffect } from "react";
import SearchComponent from "../../../components/common/SearchComponent";
import MemberMgtView from "./MemberMgtView";

//관리자용 회원 관리 페이지
export default function MemberManagement() {
  const [memberList, setMemberList] = useState([]);
  const [open, setOpen] = useState(false);

  //상세 조회할 회원 저장 state
  const [selectedMemberInfo, setSelectedMemberInfo] = useState({});

  const toggleDrawer = (open, member) => (event) => {
    if (event.type === "keydown" && (event.key === "Tab" || event.key === "Shift")) {
      return;
    }
    setSelectedMemberInfo(member);
    setOpen(open);
  };
  //검색 조건 설정 state
  const [pageParameter, setPageParameter] = useState({
    page: 1,
    size: 15,
    type: "name",
    keyword: "",
    totalPages: 1,
  });
  //page state 변경 시, 새 목록 조회
  useEffect(() => {
    getMemberPage(pageParameter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageParameter.page]);

  //회원목록 조회 요청
  const getMemberPage = async (pageParameter) => {
    try {
      const { page, size, type, keyword } = pageParameter;
      const response = await jwtAxios.get(`http://localhost:8080/members`, {
        params: {
          page: page,
          size: size,
          type: type,
          keyword: keyword,
        },
      });
      if (response.status === 200) {
        if (response.data.totalPages > 0) {
          console.log(response.data);
          setMemberList(response.data.content);
          setPageParameter({ ...pageParameter, totalPages: response.data.totalPages, page: response.data.number + 1 });
        } else {
          setMemberList([]);
          setPageParameter({ ...pageParameter, page: 1, totalPages: 1 });
        }
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //검색 조건 반영
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
            회원정보관리
          </Typography>
          <SearchComponent handleChange={handleChange} pageParameter={pageParameter} doSearch={getMemberPage} target={"member"} />
        </Box>
        <Grid container spacing={2}>
          {memberList
            ? memberList.map((member) => (
                <Grid key={member.memberId} item xs={4} minWidth={350}>
                  <Card onClick={toggleDrawer(true, member)} sx={{ cursor: "pointer" }}>
                    <CardContent>
                      <Box>
                        <Avatar
                          alt={member.name}
                          src={member.profileImage ? `http://localhost:8080/members/${member.memberId}/profileImage/${member.profileImage}` : ""}
                          sx={{ width: 70, height: 70, margin: "auto" }}
                        />
                        <Typography variant="h6" align="center">
                          {member.name}
                        </Typography>
                        <Typography variant="body1" align="center" fontSize={"small"}>
                          ({member.roles.includes("ROLE_ADMIN") ? "관리자" : "일반회원"})
                        </Typography>
                      </Box>

                      <Box align="left" sx={{ ml: 4 }}>
                        <Typography variant="body1">이메일: {member.email}</Typography>
                        <Typography variant="body1">전화번호: {member.phone}</Typography>
                        <Typography variant="body1">성별: {member.gender}</Typography>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))
            : null}
        </Grid>
        <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
          <BasicPagination pageParameter={pageParameter} page={pageParameter.page} handleChange={handleChange} />
        </Box>
      </Container>
      <Drawer anchor="right" open={open} onClose={toggleDrawer(false, null)}>
        <MemberMgtView setOpen={setOpen} memberInfo={selectedMemberInfo} getMemberPage={getMemberPage} pageParameter={pageParameter} />
      </Drawer>
    </>
  );
}
