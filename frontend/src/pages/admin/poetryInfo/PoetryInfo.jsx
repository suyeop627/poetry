import React, { useEffect } from "react";
import {
  Typography,
  Container,
  Table,
  TableContainer,
  TableHead,
  Paper,
  TableRow,
  TableCell,
  TableBody,
  Box,
  Divider,
  List,
  ListItem,
  Button,
} from "@mui/material";
import jwtAxios from "../../../auth/jwtAxios";
import { useState } from "react";
import FormattedDate from "../../../components/common/FormattedDate";

//서비스 관련 통계 및 관리 정보 표출
export default function PoetryInfo() {
  const [statistics, setStatistics] = useState({
    memberStatistics: {
      totalMembersCount: "",
      newMembersCountOfRecentMonth: "",
      monthlyNewMembersCount: "",
    },
    adminList: {},
    poemStatistics: [],
  });

  const [loadData, setLoadData] = useState(false);

  //페이지 로드시 데이터 조회
  useEffect(() => {
    setLoadData(false);
    getStatistics();
  }, []);

  //데이터 조회 요청
  const getStatistics = async () => {
    try {
      const response = await jwtAxios.get("http://localhost:8080/admin/report");
      if (response.status === 200) {
        setStatistics(response.data);
        setLoadData(true);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //엑셀 다운로드 요청
  const download = async () => {
    try {
      const response = await jwtAxios.get("http://localhost:8080/admin/downloadStatistics", { responseType: "blob" });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "poetry.xlsx");
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <Container sx={{ minWidth: 500, mb: 10 }}>
      {loadData ? (
        <>
          <Box sx={{ mt: 10, display: "flex", justifyContent: "flex-end" }}>
            <Button onClick={download} variant="contained">
              엑셀 다운로드
            </Button>
          </Box>

          <Box>
            <Typography variant="h5" gutterBottom>
              회원 현황
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <List spacing={2}>
              <ListItem>
                <Typography variant="h6" gutterBottom>
                  총 회원 수: {statistics.memberStatistics.totalMembersCount} 명
                </Typography>
              </ListItem>
              <ListItem>
                <Typography variant="h6" gutterBottom>
                  최근 1개월 가입자 수 : {statistics.memberStatistics.newMembersCountOfRecentMonth} 명
                </Typography>
              </ListItem>
              <ListItem>
                <Typography variant="h6" gutterBottom>
                  최근 1년 간 월 평균 가입자 수 : {statistics.memberStatistics.monthlyNewMembersCount} 명
                </Typography>
              </ListItem>
            </List>
          </Box>
          <Box sx={{ mt: 10 }}>
            <Typography variant="h5" gutterBottom>
              관리자 현황({statistics.adminList.length} 명)
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell align="center">ID</TableCell>
                    <TableCell align="center">이름</TableCell>
                    <TableCell align="center">이메일</TableCell>
                    <TableCell align="center">연락처</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {statistics.adminList.map((admin, index) => (
                    <TableRow key={index}>
                      <TableCell align="center">{admin.memberId}</TableCell>
                      <TableCell align="center">{admin.name}</TableCell>
                      <TableCell align="center">{admin.email}</TableCell>
                      <TableCell align="center">{admin.phone}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
          <Box sx={{ mt: 10 }}>
            <Typography variant="h5" gutterBottom>
              카테고리 별 신규 게시글 현황
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell align="center">카테고리</TableCell>
                    <TableCell align="center">오늘</TableCell>
                    <TableCell align="center">최근 1개월</TableCell>
                    <TableCell align="center">최근 3개월</TableCell>
                    <TableCell align="center">최근 6개월</TableCell>
                    <TableCell align="center">최근 1년</TableCell>
                    <TableCell align="center">총</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {statistics.poemStatistics.map((statistics) => (
                    <TableRow key={statistics.categoryId}>
                      <TableCell align="center">{statistics.categoryName}</TableCell>
                      <TableCell align="center">{statistics.poemsTodayCount}</TableCell>
                      <TableCell align="center">{statistics.poemsLastMonthCount}</TableCell>
                      <TableCell align="center">{statistics.poemsLast3MonthsCount}</TableCell>
                      <TableCell align="center">{statistics.poemsLast6MonthsCount}</TableCell>
                      <TableCell align="center">{statistics.poemsLastYearCount}</TableCell>
                      <TableCell align="center">{statistics.totalPoemsCount}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
          <Box sx={{ mt: 10 }}>
            <Typography variant="h5" gutterBottom>
              이용 제한 회원 현황({statistics.restrictedMemberList.length} 명)
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell align="center" width={"15%"}>
                      ID
                    </TableCell>
                    <TableCell align="center" width={"15%"}>
                      이름
                    </TableCell>
                    <TableCell align="center" width={"15%"}>
                      이메일
                    </TableCell>
                    <TableCell align="center" width={"15%"}>
                      제한 시작일
                    </TableCell>
                    <TableCell align="center" width={"15%"}>
                      제한 종료일
                    </TableCell>
                    <TableCell align="center" width={"25%"}>
                      제한 사유
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {statistics.restrictedMemberList.map((member, index) => (
                    <TableRow key={index}>
                      <TableCell align="center" width={"15%"}>
                        {member.memberId}
                      </TableCell>
                      <TableCell align="center" width={"15%"}>
                        {member.name}
                      </TableCell>
                      <TableCell align="center" width={"15%"}>
                        {member.email}
                      </TableCell>
                      <TableCell align="center" width={"15%"}>
                        <FormattedDate localDateTime={member.restrictionStartDate} type="date" />
                      </TableCell>
                      <TableCell align="center" width={"15%"}>
                        <FormattedDate localDateTime={member.restrictionEndDate} type="date" />
                      </TableCell>
                      <TableCell align="center" width={"25%"}>
                        {member.restrictionReason}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </>
      ) : null}
    </Container>
  );
}
