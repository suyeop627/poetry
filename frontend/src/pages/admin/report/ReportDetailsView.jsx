import React, { useEffect } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

import { useState } from "react";
import {
  Box,
  Button,
  Divider,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";

import { SearchOutlined } from "@mui/icons-material";
import jwtAxios from "../../../auth/jwtAxios";
import UserAvatar from "../../../components/common/UserAvatar";
import FormattedDate from "../../../components/common/FormattedDate";

//신고 상세 정보 표출
const ReportDetailsView = ({ categories }) => {
  const { reportId } = useParams();
  const [poem, setPoem] = useState({});
  const [poemSettings, setPoemSettings] = useState({});

  //회원 신고 사유별 신고횟수
  const [reportDetailsStatistics, setReportDetailsStatistics] = useState({
    poemIrrelevantCategoryReportCount: "",
    poemContentReportCount: "",
    poemBackgroundImageReportCount: "",
    memberProfileImageReportCount: "",
    memberNameReportCount: "",
  });
  //신고 내역 정보
  const [report, setReport] = useState({
    creationDate: "",
    doneByEmail: "",
    donByName: "",
    poemId: "",
    memberRestrictionEndDate: "",
  });
  const [reportDetails, setReportDetails] = useState([]);
  const navigate = useNavigate();

  const { state } = useLocation();

  const [backgroundSrc, setBackgroundSrc] = useState("");
  const [profileSrc, setProfileSrc] = useState("");

  const [loadData, setLoadData] = useState(false);

  //신고 처리 내역
  const [reportHandleResult, setReportHandleResult] = useState({
    categoryId: poem.categoryId,
    profileImageDeletion: false,
    backgroundDeletion: false,
    memberRestrictionAddDate: 0,
    restrictionReason: "",
    writerName: "",
    deleted: false,
  });

  //신고 상세 내역 조회 요청
  const getReportDetails = async () => {
    try {
      setLoadData(false);
      const response = await jwtAxios.get(`http://localhost:8080/reports/${reportId}`);
      if (response.status === 200) {
        const {
          creationDate,
          doneByEmail,
          donByName,
          poemId,
          poemSettings,
          poem,
          reportDetails,
          reportDetailsStatistics,
          memberRestrictionEndDate,
          restrictionReason,
        } = response.data;

        setReport({
          creationDate: creationDate,
          doneByEmail: doneByEmail,
          donByName: donByName,
          poemId: poemId,
          memberRestrictionEndDate: memberRestrictionEndDate,
        });

        setPoem(poem);
        setPoemSettings(poemSettings);
        setReportDetails(reportDetails);
        setReportDetailsStatistics(reportDetailsStatistics);

        setProfileSrc(poem.profileImage);
        setBackgroundSrc(`http://localhost:8080/poems/${poem.poemId}/backgroundImage/${poemSettings.backgroundImage}`);

        setReportHandleResult({
          ...reportHandleResult,
          categoryId: poem.categoryId,
          restrictionReason: restrictionReason ? restrictionReason : "",
          writerName: poem.name,
          deleted: poem.deleted,
        });
        setLoadData(true);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  useEffect(() => {
    getReportDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reportId]);

  const { title, content, description, view, name } = poem;
  const { fontFamily, titleFontSize, contentFontSize, color, textAlign, backgroundOpacity } = poemSettings;

  //게시글 내용 줄바꿈 처리
  useEffect(() => {
    if (loadData && typeof content === "string") {
      const contentWithLineBreaks = content.split("\n").map((line, index) => (
        <React.Fragment key={index}>
          {line}
          <br />
        </React.Fragment>
      ));

      setPoem({ ...poem, content: contentWithLineBreaks });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadData]);

  //신고 처리 내역 변경사항 반영
  const handleReportHandleResultChange = (event) => {
    const { name, value } = event.target;
    setReportHandleResult({
      ...reportHandleResult,
      [name]: value,
    });
  };

  //신고처리 내역 중 boolean 토글 변경 처리
  const handleBooleanToggleChange = (event, value) => {
    const { id } = event.target;
    setReportHandleResult({
      ...reportHandleResult,
      [id]: !value,
    });
  };

  //회원 서비스 이용제한 사유 반영
  const handleRestrictionReason = (event) => {
    const { name, value } = event.target;
    setReportHandleResult({
      ...reportHandleResult,
      [name]: value,
    });
  };

  //회원 이름 변경
  const handleRename = () => {
    let tempWriterName = "";
    if (reportHandleResult.writerName === name) {
      tempWriterName = "Poe-try user";
    } else {
      tempWriterName = name;
    }
    setReportHandleResult({
      ...reportHandleResult,
      writerName: tempWriterName,
    });
  };

  //신고 처리내역 저장요청
  const handleSubmit = async () => {
    try {
      if (reportHandleResult.memberRestrictionAddDate > 0) {
        if (!reportHandleResult.restrictionReason) {
          alert("로그인 제한 시, 제한 사유 입력은 필수입니다.");
          return;
        }
      }
      const response = await jwtAxios.patch(`http://localhost:8080/reports/${reportId}`, reportHandleResult);
      if (response.status === 200) {
        alert("신고 대상에 대한 처리가 정상적으로 저장됐습니다.");
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //신고 내역 삭제 요청
  const handleDeleteReport = async () => {
    try {
      const response = await jwtAxios.delete(`http://localhost:8080/reports/${reportId}`);
      if (response.status === 200) {
        alert("신고 내역을 삭제했습니다.");
        navigate("/admin/reports", { state: state });
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <>
      {loadData ? (
        <Box sx={{ width: "70%", mt: 5, minWidth: 1100 }}>
          <Typography variant="h4" sx={{ mb: 3 }}>
            신고 내역
          </Typography>
          <Box>
            <Typography variant="h6" sx={{ mb: 3 }}>
              신고 사유
              <Typography fontSize={"small"} sx={{ ml: 2 }}>
                (총 신고횟수: {reportDetails.length})
              </Typography>
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell width={"20%"} align="center">
                      카테고리와 <br />
                      관련 없는 내용
                    </TableCell>
                    <TableCell width={"20%"} align="center">
                      불건전한 내용
                    </TableCell>
                    <TableCell width={"20%"} align="center">
                      불건전한 <br />
                      배경 이미지
                    </TableCell>
                    <TableCell width={"20%"} align="center">
                      불건전한 회원
                      <br /> 프로필 이미지
                    </TableCell>
                    <TableCell width={"20%"} align="center">
                      불건전한 회원 이름
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow>
                    <TableCell align="center" width={"20%"}>
                      {reportDetailsStatistics?.poemIrrelevantCategoryReportCount}
                    </TableCell>
                    <TableCell align="center" width={"20%"}>
                      {reportDetailsStatistics?.poemContentReportCount}
                    </TableCell>
                    <TableCell align="center" width={"20%"}>
                      {reportDetailsStatistics?.poemBackgroundImageReportCount}
                    </TableCell>
                    <TableCell align="center" width={"20%"}>
                      {reportDetailsStatistics?.memberProfileImageReportCount}
                    </TableCell>
                    <TableCell align="center" width={"20%"}>
                      {reportDetailsStatistics?.memberNameReportCount}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
          <Divider sx={{ mt: 3, mb: 3 }} />
          <Grid container spacing={10} justifyContent={"space-between"}>
            <Grid item xs={6}>
              <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
                신고 상세
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <TableContainer component={Paper} sx={{ maxHeight: 1050 }}>
                <Table stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell width={"30%"} align="center">
                        신고일시
                      </TableCell>
                      <TableCell width={"20%"} align="center">
                        신고자
                      </TableCell>
                      <TableCell width={"50%"} align="center">
                        신고 사유
                      </TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {reportDetails
                      .sort((a, b) => new Date(b.reportDate) - new Date(a.reportDate))
                      .map((details) => (
                        <TableRow key={details.reportDetailsId}>
                          <TableCell align="center" width={"30%"}>
                            <FormattedDate localDateTime={details.reportDate} type={"time"} />
                          </TableCell>
                          <TableCell align="center" width={"20%"}>
                            {details.name}
                          </TableCell>
                          <TableCell align="left" width={"50%"}>
                            {details.reportReasons.map((reason, index) => (
                              <Typography fontSize={13} key={index}>
                                ·{reason}
                              </Typography>
                            ))}
                            <Divider sx={{ mb: 1, mt: 1 }} />
                            <Typography>{details.reportComment}</Typography>
                          </TableCell>
                        </TableRow>
                      ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Grid>
            <Grid item xs={6}>
              <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
                신고 대상
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box>
                <Typography variant="body1">회원 정보</Typography>
                <Grid container>
                  <Grid item xs={6} sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                    <UserAvatar memberId={poem.memberId} profileImage={reportHandleResult.profileImageDeletion ? "" : profileSrc} name={poem.name} />
                  </Grid>
                  <Grid item xs={6} sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                    <Typography>{reportHandleResult.writerName}</Typography>
                  </Grid>
                  <Grid item xs={6} sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                    {profileSrc ? (
                      <Button
                        variant="outlined"
                        color={reportHandleResult.profileImageDeletion ? "info" : "error"}
                        id="profileImageDeletion"
                        onClick={(event) => handleBooleanToggleChange(event, reportHandleResult.profileImageDeletion)}
                        sx={{ ml: 1 }}
                      >
                        {reportHandleResult.profileImageDeletion ? "삭제 취소" : "프로필 사진 삭제"}
                      </Button>
                    ) : (
                      <Button variant="outlined" disabled>
                        프로필사진 미등록
                      </Button>
                    )}
                  </Grid>
                  <Grid item xs={6} sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                    <Button
                      variant="outlined"
                      color={reportHandleResult.writerName === poem.name ? "error" : "info"}
                      onClick={handleRename}
                      sx={{ ml: 1 }}
                    >
                      {reportHandleResult.writerName === poem.name ? "회원 이름 변경" : "변경 취소"}
                    </Button>
                  </Grid>
                </Grid>
              </Box>
              <Box>
                <Typography sx={{ mt: 2 }} variant={"body1"}>
                  기존 제한일 :{" "}
                  {report?.memberRestrictionEndDate ? <FormattedDate localDateTime={report.memberRestrictionEndDate} type={"date"} /> : "제한없음"}
                </Typography>
                <FormControl sx={{ mt: 2, mb: 1, width: 300 }}>
                  <InputLabel id="category" htmlFor="font-family" sx={{ backgroundColor: "white" }}>
                    로그인 제한 설정
                  </InputLabel>
                  <Select
                    labelId="category"
                    value={reportHandleResult.memberRestrictionAddDate}
                    onChange={handleReportHandleResultChange}
                    inputProps={{
                      name: "memberRestrictionAddDate",
                      id: "memberRestrictionAddDate",
                    }}
                    size="small"
                  >
                    <MenuItem value={0}>추가 제한일 미지정</MenuItem>
                    <MenuItem value={3}>+3일</MenuItem>
                    <MenuItem value={7}>+7일</MenuItem>
                    <MenuItem value={30}>+30일</MenuItem>
                    <MenuItem value={90}>+90일</MenuItem>
                    <MenuItem value={9999}>+9999일</MenuItem>
                    <MenuItem value={-1}>제한 해제</MenuItem>
                  </Select>
                </FormControl>
                {reportHandleResult.memberRestrictionAddDate > 0 ? (
                  <Box>
                    <Typography sx={{ mt: 2 }}>로그인 제한 사유 :</Typography>
                    <TextField
                      size="small"
                      fullWidth
                      inputProps={{ maxLength: 100 }}
                      multiline
                      rows={2}
                      name="restrictionReason"
                      value={reportHandleResult.restrictionReason}
                      onChange={handleRestrictionReason}
                    />
                  </Box>
                ) : null}
              </Box>
              <Divider sx={{ mt: 3, mb: 2 }} />
              <Box>
                <Typography variant="body1">시 정보</Typography>
                <Box sx={{ display: "flex", flexDirection: "column", flexWrap: "wrap", alignItems: "center" }}>
                  <Box sx={{ width: 550 }}>
                    <FormControl fullWidth sx={{ mt: 2, mb: 5 }}>
                      <InputLabel id="category" htmlFor="font-family" sx={{ backgroundColor: "white" }}>
                        카테고리
                      </InputLabel>
                      <Select
                        labelId="category"
                        value={reportHandleResult.categoryId}
                        onChange={handleReportHandleResultChange}
                        inputProps={{
                          name: "categoryId",
                          id: "categoryId",
                        }}
                        size="small"
                      >
                        {categories.map((category) => (
                          <MenuItem key={category.categoryId} value={category.categoryId}>
                            {category.categoryName}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                    <Typography variant="body1">설명 : {description}</Typography>
                  </Box>

                  <Box
                    sx={{
                      mt: 2,
                      height: 700,
                      width: 550,
                      wordBreak: "break-all",
                      backgroundSize: "cover",
                      backgroundPosition: "center",
                      backgroundRepeat: "no-repeat",
                      backgroundImage: `linear-gradient(rgba(255,255,255,${backgroundOpacity}), rgba(255,255,255,${backgroundOpacity})), 
                      url("${reportHandleResult.backgroundDeletion ? "" : backgroundSrc}")`,
                      boxShadow: "0px 0px 5px 2px rgba(0,0,0,0.2)",
                    }}
                  >
                    <Box component="div" sx={{ p: 5 }}>
                      <Typography variant={titleFontSize} fontFamily={fontFamily} color={color} textAlign={textAlign} sx={{ mt: 10 }}>
                        {title}
                      </Typography>
                      <br />

                      <Typography
                        variant="body1"
                        fontFamily={fontFamily}
                        fontSize={contentFontSize}
                        color={color}
                        textAlign={textAlign}
                        sx={{ mt: 3 }}
                      >
                        {content}
                      </Typography>
                      <br />
                      <br />
                      <br />
                    </Box>
                  </Box>
                </Box>
                <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 3 }}>
                  <Typography sx={{ mr: 2 }}>
                    <SearchOutlined /> {view}
                  </Typography>
                </Box>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2, mt: 3 }}>
                  {poemSettings?.backgroundImage ? (
                    <Button
                      variant="outlined"
                      id="backgroundDeletion"
                      color={reportHandleResult.backgroundDeletion ? "info" : "error"}
                      onClick={(event) => handleBooleanToggleChange(event, reportHandleResult.backgroundDeletion)}
                      sx={{ ml: 1 }}
                    >
                      {reportHandleResult.backgroundDeletion ? "삭제 취소" : "배경 이미지 삭제"}
                    </Button>
                  ) : (
                    <Button variant="outlined" disabled>
                      배경 이미지 미등록
                    </Button>
                  )}

                  <Button
                    variant="outlined"
                    color={reportHandleResult.deleted ? "info" : "error"}
                    id={"deleted"}
                    onClick={(event) => handleBooleanToggleChange(event, reportHandleResult.deleted)}
                    sx={{ ml: 1 }}
                  >
                    {reportHandleResult.deleted ? "시 삭제 취소" : "시 삭제"}
                  </Button>
                </Box>
                <Box display={"flex"}>
                  <Typography variant="caption">*시 삭제 : 신고내역 삭제 시, 해당 시를 삭제할 지 여부를 선택합니다.</Typography>
                </Box>
              </Box>
            </Grid>
          </Grid>
          <Divider sx={{ mb: 2, mt: 2 }} />
          <Box sx={{ display: "flex", justifyContent: "space-around", mb: 2, mt: 3 }}>
            <Button variant="contained" color="info" onClick={() => navigate("/admin/reports", { state: state })}>
              뒤로가기
            </Button>
            <Button variant="contained" color="success" onClick={handleSubmit} sx={{ ml: 1 }}>
              신고 처리 결과 저장
            </Button>
            <Button variant="contained" color="error" onClick={handleDeleteReport} sx={{ ml: 1 }}>
              신고 내역 삭제
            </Button>
          </Box>
        </Box>
      ) : null}
    </>
  );
};

export default ReportDetailsView;
