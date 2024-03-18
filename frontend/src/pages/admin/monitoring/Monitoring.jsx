import React, { useCallback, useEffect } from "react";
import { Typography, Container, Box, List, ListItem, Button, TextField, Grid, ListItemButton, Paper, Divider } from "@mui/material";
import jwtAxios from "../../../auth/jwtAxios";
import { useState } from "react";

//서비스 관련 통계 및 관리 정보 표출
export default function Monitoring() {
  const [logDate, setLogdate] = useState(new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2));

  //날짜 입력값 변경 처리
  const handleDateChange = (event) => {
    const { value } = event.target;
    setLogdate(value);
    setLogData("");
    setSelectedLog("");
  };

  const [failedToDeleteFileLogs, setFailedToDeleteFileLogs] = useState([]);
  const [internalServerErrorLogs, setInternalServerErrorLogs] = useState([]);
  const [selectedLog, setSelectedLog] = useState();
  const [logData, setLogData] = useState("");

  //로그 파일의 데이터에 개행 적용
  const logDataWithLineBreaks = logData?.split("\n").map((line, index) => (
    <React.Fragment key={index}>
      {line}
      <br />
    </React.Fragment>
  ));

  //선택한 월의 로그파일 목록 조회
  const getLogFilesOfThisMonth = useCallback(async () => {
    try {
      const searchMonth = logDate.replace("-", "_");
      const response = await jwtAxios.get(`http://localhost:8080/admin/logs/${searchMonth}`);
      if (response.status === 200) {
        setFailedToDeleteFileLogs(response.data.failedToDeleteFileLogs);
        setInternalServerErrorLogs(response.data.internalServerErrorLogs);
      }
    } catch (error) {
      if (error.response.status === 404) {
        setFailedToDeleteFileLogs([]);
        setInternalServerErrorLogs([]);
      } else {
        alert("요청 처리중 오류가 발생했습니다.");
      }
    }
  }, [logDate]);

  //선택한 파일의 로그파일 데이터 조회
  const showFile = async (fileName) => {
    try {
      setSelectedLog(fileName);
      console.log(selectedLog);
      const searchMonth = logDate.replace("-", "_");
      const response = await jwtAxios.get(`http://localhost:8080/admin/logs/${searchMonth}/${fileName}`);
      setLogData(response.data);
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //월 변경에따라 로그파일 목록 조회
  useEffect(() => {
    getLogFilesOfThisMonth();
  }, [getLogFilesOfThisMonth]);

  // const test = async () => {
  //   await jwtAxios.get("http://localhost:8080/admin/test");
  // };

  // useEffect(() => {
  //   for (let i = 0; i < 500; i++) {
  //     test();
  //   }
  // }, []);

  //선택한 월의 로그파일 다운로드 요청
  const downloadLogFiles = async () => {
    try {
      const searchMonth = logDate.replace("-", "_");
      const response = await jwtAxios.get(`http://localhost:8080/admin/logs/${searchMonth}/download`, { responseType: "blob" });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `${searchMonth}_logs.zip`);
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      if (error.response.status === 404) {
        alert("선택하신 기간에 생성된 로그파일이 없습니다.");
        return;
      }
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  return (
    <Container sx={{ minWidth: 500, mt: 10 }}>
      <Typography variant="h4" gutterBottom>
        로그 내역
      </Typography>
      <Divider sx={{ mb: 2 }} />
      <Box sx={{ display: "flex", justifyContent: "space-between" }}>
        <TextField
          name="logDate"
          label="날짜"
          type="month"
          value={logDate}
          onChange={handleDateChange}
          InputLabelProps={{
            shrink: true,
          }}
          size="small"
        />
        <Button variant="contained" onClick={downloadLogFiles}>
          다운로드
        </Button>
      </Box>

      <Grid container sx={{ mt: 3 }} justifyContent={"space-around"}>
        <Grid item xs={3}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            · 파일 삭제 실패 로그
          </Typography>
          <List sx={{ overflow: "auto", maxHeight: "25vh" }} component={Paper}>
            {failedToDeleteFileLogs.length > 0 ? (
              failedToDeleteFileLogs.map((logfile) => (
                <ListItem key={logfile} size="small" disablePadding>
                  <ListItemButton onClick={() => showFile(logfile)} dense>
                    <Typography sx={{ fontWeight: selectedLog === logfile ? "bold" : "" }} fontSize={"small"}>
                      {logfile}
                    </Typography>
                  </ListItemButton>
                </ListItem>
              ))
            ) : (
              <ListItem>로그가 없습니다.</ListItem>
            )}
          </List>
          <Divider sx={{ width: "100%", mt: 5, mb: 2 }} />
          <Typography variant="h6" sx={{ mb: 2 }}>
            · 서버 오류 로그
          </Typography>
          <List sx={{ overflow: "auto", maxHeight: "25vh" }} component={Paper}>
            {internalServerErrorLogs.length > 0 ? (
              internalServerErrorLogs.map((logfile) => (
                <ListItem key={logfile} size="small" disablePadding>
                  <ListItemButton onClick={() => showFile(logfile)} dense>
                    <Typography sx={{ fontWeight: selectedLog === logfile ? "bold" : "" }} fontSize={"small"}>
                      {logfile}
                    </Typography>
                  </ListItemButton>
                </ListItem>
              ))
            ) : (
              <ListItem>로그가 없습니다.</ListItem>
            )}
          </List>
        </Grid>
        {selectedLog ? (
          <Grid item xs={8} sx={{ overflowY: "auto", maxHeight: "90vh" }}>
            <Typography fontSize={"small"} sx={{ wordBreak: "break-word" }}>
              {logDataWithLineBreaks}
            </Typography>
          </Grid>
        ) : (
          <Grid item xs={7}></Grid>
        )}
      </Grid>
    </Container>
  );
}
