import * as React from "react";
import { Box, Typography, Container, Divider, TableContainer, Paper, Table, TableHead, TableRow, TableCell, TableBody, Button } from "@mui/material";
import { useState } from "react";
import { useEffect } from "react";
import jwtAxios from "../../../auth/jwtAxios";
import BasicPagination from "../../components/BasicPagination";
import FormattedDate from "../../../components/common/FormattedDate";
import { useLocation, useNavigate } from "react-router-dom";
import ReportSearch from "./ReportSearch";

//신고내역 목록 표출
export default function ReportManagement() {
  const [reportList, setReportList] = useState([]);
  const { state } = useLocation();

  //현재 날짜 계산(초기 검색 조건 날짜범위 설정)
  const getCurrentDate = () => {
    const currentDate = new Date();
    return currentDate.toISOString().split("T")[0];
  };
  //1년전 날짜 계산(초기 검색 조건 날짜범위 설정)
  const getAYearAgoDate = () => {
    const currentDate = new Date();
    currentDate.setFullYear(currentDate.getFullYear() - 1);
    return currentDate.toISOString().split("T")[0];
  };

  //신고 목록 검색 조건
  // 신고 상세내역에서 '뒤로가기'로 접근한 경우, 신고상세내역 페이지 이동 전 상태 그대로 표출하기 위해 전달받은 state에 따라 분기처리
  const [reportSearchParameter, setReportSearchParameter] = useState({
    page: state?.reportSearchParameter ? state.reportSearchParameter.page : 1,
    size: state?.reportSearchParameter ? state.reportSearchParameter.size : 10,
    title: state?.reportSearchParameter ? state.reportSearchParameter.title : "",
    content: state?.reportSearchParameter ? state.reportSearchParameter.content : "",
    name: state?.reportSearchParameter ? state.reportSearchParameter.name : "",
    email: state?.reportSearchParameter ? state.reportSearchParameter.email : "",
    writer: state?.reportSearchParameter ? state.reportSearchParameter.writer : "",
    reportStatus: state?.reportSearchParameter ? state.reportSearchParameter.reportStatus : "",
    startDate: state?.reportSearchParameter ? state.reportSearchParameter.startDate : getAYearAgoDate(),
    endDate: state?.reportSearchParameter ? state.reportSearchParameter.endDate : getCurrentDate(),
    orderCondition: state?.reportSearchParameter ? state.reportSearchParameter.orderCondition : "",
    direction: state?.reportSearchParameter ? state.reportSearchParameter.direction : "DESC",
    totalPages: state?.reportSearchParameter ? state.reportSearchParameter.totalPages : 1,
  });
  const navigate = useNavigate();

  //검색 조건 설정 시, 시작일과 종료일 유효성 검사
  const validateDate = () => {
    let start = reportSearchParameter.startDate;
    let end = reportSearchParameter.endDate;
    if (start === "") {
      alert("시작일을 입력해주세요.");
      handleChange("startDate", getAYearAgoDate());
    } else if (end === "") {
      alert("종료일을 입력해주세요.");
      handleChange("endDate", getCurrentDate());
    } else if (start > end) {
      alert("시작일은 종료일보다 더 클 수 없습니다.");
      handleChange("startDate", reportSearchParameter.endDate);
    }
  };

  useEffect(() => {
    validateDate();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reportSearchParameter.startDate, reportSearchParameter.endDate]);

  useEffect(() => {
    getReportRequest(reportSearchParameter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reportSearchParameter.page, reportSearchParameter.orderCondition, reportSearchParameter.direction]);

  //검색조건에 따라 신고내역 목록 조회 요청
  const getReportRequest = async (reportSearchParameter) => {
    try {
      const { page, size, title, content, name, email, writer, reportStatus, orderCondition, startDate, endDate, direction } = reportSearchParameter;
      const response = await jwtAxios.get(`http://localhost:8080/reports`, {
        params: {
          page: page,
          size: size,
          title: title,
          content: content,
          name: name,
          email: email,
          writer: writer,
          reportStatus: reportStatus,
          orderCondition: orderCondition,
          direction: direction,
          startDate: startDate,
          endDate: endDate,
        },
      });
      if (response.status === 200) {
        if (response.data.content.length > 0) {
          setReportList(response.data.content);
          setReportSearchParameter({ ...reportSearchParameter, totalPages: response.data.totalPages, page: response.data.number + 1 });
        } else {
          setReportList([]);
          setReportSearchParameter({ ...reportSearchParameter, totalPages: 1 });
        }
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  //검색조건 입력값 변경
  const handleChange = (key, value) => {
    setReportSearchParameter({
      ...reportSearchParameter,
      [key]: value,
    });
  };

  //컬럼별 오름차순 또는 내림차순 변경사항 저장
  const handleSort = (event) => {
    let column = event.target.id;
    let newDirection = "ASC";
    if (reportSearchParameter.orderCondition === column && reportSearchParameter.direction === "ASC") {
      newDirection = "DESC";
    }
    setReportSearchParameter({
      ...reportSearchParameter,
      page: 1,
      direction: newDirection,
      orderCondition: column,
    });
  };

  //오름차순 또는 내림차순에따라 화살표 표출
  const printSortArrow = (column) => {
    if (reportSearchParameter.orderCondition === column) {
      if (reportSearchParameter.direction === "DESC") {
        return " ↓";
      } else {
        return " ↑";
      }
    }
  };
  const [openSearch, setOpenSearch] = useState(false);

  //검색조건 초기화
  const resetSearchConditions = () => {
    const initObj = {
      ...reportSearchParameter,
      title: "",
      content: "",
      writer: "",
      name: "",
      email: "",
      reportStatus: "",
      startDate: getAYearAgoDate(),
      endDate: getCurrentDate(),
    };
    getReportRequest(initObj);
    setReportSearchParameter(initObj);
  };
  //검색조건 닫기
  const closeSearchForm = () => {
    resetSearchConditions();
    setOpenSearch(false);
  };

  return (
    <>
      <Container sx={{ mt: 10 }}>
        <Typography variant="h4" gutterBottom>
          신고 내역
        </Typography>
        <Divider sx={{ mb: 2 }} />
        {openSearch ? (
          <Box>
            <ReportSearch reportSearchParameter={reportSearchParameter} handleChange={handleChange} getReportRequest={getReportRequest} />
            <Box sx={{ float: "right", mb: 2, display: "flex", justifyContent: "space-between", width: 300 }}>
              <Button onClick={() => closeSearchForm()} variant="contained" color="error">
                닫기
              </Button>
              <Button variant="contained" onClick={resetSearchConditions}>
                조건초기화
              </Button>
              <Button variant="contained" onClick={() => getReportRequest(reportSearchParameter)}>
                조회
              </Button>
            </Box>

            <Divider sx={{ mt: 9 }} />
          </Box>
        ) : (
          <Box>
            <Button onClick={() => setOpenSearch(true)} variant="contained" color="warning" sx={{ float: "right", mb: 2 }}>
              검색 조건 설정
            </Button>
          </Box>
        )}

        {reportList?.length > 0 ? (
          <>
            <Box sx={{ mt: 5 }}>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell width={"15%"} align="center" id="creationDate" onClick={handleSort}>
                        신고 생성 일시{printSortArrow("creationDate")}
                      </TableCell>
                      <TableCell width={"15%"} align="center" id="title" onClick={handleSort}>
                        신고 대상 제목{printSortArrow("title")}
                      </TableCell>
                      <TableCell width={"10%"} align="center" id="reportCount" onClick={handleSort}>
                        신고 건수{printSortArrow("reportCount")}
                      </TableCell>
                      <TableCell width={"15%"} align="center" id="writer" onClick={handleSort}>
                        작성자{printSortArrow("writer")}
                      </TableCell>
                      <TableCell width={"15%"} align="center" id="reportStatus" onClick={handleSort}>
                        처리 상태{printSortArrow("reportStatus")}
                      </TableCell>
                      <TableCell width={"20%"} align="center" id="doneBy" onClick={handleSort}>
                        최종 처리자{printSortArrow("doneBy")}
                      </TableCell>
                      <TableCell width={"20%"} align="center">
                        상세
                      </TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {reportList.map((report, index) => (
                      <TableRow key={index}>
                        <TableCell align="center" width={"15%"}>
                          <FormattedDate localDateTime={report.creationDate} type={"time"} />
                        </TableCell>
                        <TableCell align="center" width={"15%"}>
                          {report.title}
                        </TableCell>
                        <TableCell align="center" width={"10%"}>
                          {report.reportCount}
                        </TableCell>
                        <TableCell align="center" width={"15%"}>
                          {report.writer}
                        </TableCell>
                        <TableCell align="center" width={"15%"}>
                          {report.reportStatus === "REPORTED" ? (
                            <span>신고접수</span>
                          ) : report.reportStatus === "UNDER_REVIEW" ? (
                            <span>검토중</span>
                          ) : (
                            <span>처리완료</span>
                          )}
                        </TableCell>
                        <TableCell align="center" width={"15%"}>
                          {report.doneBy}
                        </TableCell>
                        <TableCell align="center" width={"20%"}>
                          <Button
                            onClick={() => navigate(`/admin/reports/${report.reportId}`, { state: { reportSearchParameter } })}
                            variant="contained"
                            size={"small"}
                          >
                            상세보기
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </>
        ) : (
          <Typography variant="h6">신고된 데이터가 없습니다.</Typography>
        )}
        <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
          <BasicPagination pageParameter={reportSearchParameter} page={reportSearchParameter.page} handleChange={handleChange} />
        </Box>
      </Container>
    </>
  );
}
