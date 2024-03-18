import React, { useEffect } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import jwtAxios from "../../../auth/jwtAxios";
import { useState } from "react";
import { Box, Button, Divider, Typography } from "@mui/material";
import UserAvatar from "../../../components/common/UserAvatar";
import { useSelector } from "react-redux";
import { selectAuth } from "../../../auth/authSlice";
import axios from "axios";
import { SearchOutlined } from "@mui/icons-material";
import ReportModal from "../../components/ReportModal";
import BookmarkModal from "./BookmarkModal";
import CategoryNameFromId from "../../../components/common/CategoryNameFromId";

//게시글 상세보기 페이지
const ViewPoem = ({ categories }) => {
  const { poemId } = useParams();
  const [poem, setPoem] = useState({});
  const [poemSettings, setPoemSettings] = useState({});
  const { isLoggedIn, user } = useSelector(selectAuth);
  const { state } = useLocation();
  const { fromUrl } = state;
  const navigate = useNavigate();
  useEffect(() => {
    getPoemDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [poemId]);
  const [backgroundSrc, setBackgroundSrc] = useState("");

  const [loadData, setLoadData] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(false);
  //게시글 상세 조회 요청
  const getPoemDetails = async () => {
    try {
      setLoadData(false);
      let response;
      if (isLoggedIn) {
        response = await jwtAxios.get(`http://localhost:8080/poems/${poemId}`);
      } else {
        response = await axios.get(`http://localhost:8080/poems/${poemId}`);
      }

      if (response.status === 200) {
        setPoem(response.data.poem);
        setPoemSettings(response.data.poemSettings);
        setIsBookmarked(response.data.isBookmarked);
        setLoadData(true);
        if (response.data.poemSettings.backgroundImage) {
          const src = `http://localhost:8080/poems/${poemId}/backgroundImage/${response.data.poemSettings.backgroundImage}`;
          setBackgroundSrc(src);
        }
      }
    } catch (error) {
      if (error.response.status === 404) {
        alert("존재하지 않는 게시물입니다.");
        navigate(-1);
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
        navigate(-1);
      }
    }
  };
  const { title, content, description, view, name, bookmarkMemberList, categoryId } = poem;
  const { fontFamily, titleFontSize, contentFontSize, color, textAlign, backgroundOpacity } = poemSettings;

  //게시글 삭제 요청
  const handleDeletePoem = async () => {
    try {
      const isPoemDelete = window.confirm("삭제하시겠습니까?");
      if (!isPoemDelete) {
        return;
      }
      const response = await jwtAxios.delete(`http://localhost:8080/poems/${poemId}`);
      if (response.status === 200) {
        alert("삭제됐습니다.");
        navigate(-1);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //본문에 개행문자 줄바꿈으로 적용
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

  //게시글 북마크 요청
  const handleBookmarkPoem = async () => {
    try {
      if (!isLoggedIn) {
        alert("로그인 한 회원만 이용가능한 기능입니다.");
        return;
      }
      const response = await jwtAxios.post(`http://localhost:8080/poems/${poemId}/bookmark`);
      if (response.status === 200) {
        alert("이 시를 담았습니다. \n담은 시는 마이페이지에서 모아보실 수 있습니다.");
        getPoemDetails();
      }
    } catch (error) {
      if (error.response.status === 409) {
        alert("이미 담아간 시입니다.");
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };

  //게시글 북마크 취소 요청
  const handleBookmarkCancellation = async () => {
    try {
      if (!isLoggedIn) {
        alert("로그인 한 회원만 이용가능한 기능입니다.");
        return;
      }
      const response = await jwtAxios.delete(`http://localhost:8080/poems/${poemId}/bookmark`);
      if (response.status === 200) {
        alert("북마크를 취소했습니다.");
        getPoemDetails();
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  const [reportModalOpen, setReportModalOpen] = useState(false);
  const handleReportModalOpen = () => setReportModalOpen(true);
  const handleReportModalClose = () => setReportModalOpen(false);

  //기존 신고한 게시글인지 확인 요청
  const checkIsReportedAlready = async () => {
    try {
      if (!isLoggedIn) {
        alert("로그인 한 회원만 이용가능한 기능입니다.");
        return;
      }
      const response = await jwtAxios.get(`http://localhost:8080/reports/${poemId}/check`);
      if (response.status === 200) {
        handleReportModalOpen(true);
        return;
      }
    } catch (error) {
      if (error.response.status === 409) {
        alert("이미 신고한 게시글입니다.");
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };
  return (
    <>
      {loadData ? (
        <Box sx={{ width: 700, pt: 3 }}>
          <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", flexDirection: "column" }}>
            <UserAvatar memberId={poem.memberId} profileImage={poem.profileImage} name={poem.name} />
            <Typography>{name}</Typography>
          </Box>
          <Box sx={{ display: "flex", justifyContent: "space-between", mt: 3 }}>
            <CategoryNameFromId categories={categories} id={categoryId} />

            {poem.memberId && user?.memberId === poem.memberId ? (
              <Box sx={{ display: "flex", justifyContent: "flex-end", mb: 2 }}>
                <Button variant="contained" component={Link} to={`/poems/${poemId}/edit`}>
                  수정하기
                </Button>
                <Button variant="contained" color="error" onClick={handleDeletePoem} sx={{ ml: 1 }}>
                  삭제하기
                </Button>
              </Box>
            ) : (
              <>
                <Box sx={{ display: "flex", justifyContent: "flex-end", mb: 2 }}>
                  {isBookmarked ? (
                    <Button sx={{ mr: 3 }} variant="contained" onClick={handleBookmarkCancellation}>
                      담기 취소
                    </Button>
                  ) : (
                    <Button sx={{ mr: 3 }} variant="contained" onClick={handleBookmarkPoem}>
                      담기
                    </Button>
                  )}

                  <ReportModal
                    reportModalOpen={reportModalOpen}
                    handleReportModalClose={handleReportModalClose}
                    poemId={poemId}
                    checkIsReportedAlready={checkIsReportedAlready}
                  />
                </Box>
              </>
            )}
          </Box>
          <Divider />
          <Box sx={{ display: "flex", flexDirection: "column", flexWrap: "wrap", alignItems: "center" }}>
            <Box
              sx={{
                mt: 5,
                minHeight: 700,
                width: 550,
                wordBreak: "break-all",
                backgroundSize: "cover",
                backgroundPosition: "center",
                backgroundRepeat: "no-repeat",
                backgroundImage: `linear-gradient(rgba(255,255,255,${backgroundOpacity}), rgba(255,255,255,${backgroundOpacity})), url("${backgroundSrc}")`,
                boxShadow: "0px 0px 5px 2px rgba(0,0,0,0.2)",
              }}
            >
              <Box component="div" sx={{ p: 5 }}>
                <Typography variant={titleFontSize} fontFamily={fontFamily} color={color} textAlign={textAlign} sx={{ mt: 10 }}>
                  {title}
                </Typography>
                <br />

                <Typography variant="body1" fontFamily={fontFamily} fontSize={contentFontSize} color={color} textAlign={textAlign} sx={{ mt: 3 }}>
                  {content}
                </Typography>
                <br />
                <br />
                <br />
              </Box>
            </Box>
            <Typography sx={{ mt: 5 }}>{description}</Typography>
          </Box>

          <Divider sx={{ mt: 5 }} />
          <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
            <Typography sx={{ mr: 2 }}>
              <SearchOutlined /> {view}
            </Typography>
            <BookmarkModal bookmarkMemberList={bookmarkMemberList} />
          </Box>
          <Button
            variant="contained"
            onClick={() => {
              state.pageParameter ? navigate(fromUrl, { state: state }) : navigate(fromUrl);
            }}
          >
            {state.pageParameter ? "뒤로가기" : "목록으로"}
          </Button>
        </Box>
      ) : null}
    </>
  );
};

export default ViewPoem;
