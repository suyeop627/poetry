import { Box, Divider, Paper, Typography } from "@mui/material";
import axios from "axios";
import React, { useEffect, useState } from "react";
import UserAvatar from "../components/common/UserAvatar";

//메인페이지
const MainPage = () => {
  const [poem, setPoem] = useState({});
  const [poemSettings, setPoemSettings] = useState({});
  const [loadData, setLoadData] = useState(false);
  useEffect(() => {
    getMainContent();
  }, []);
  const [isExistPoem, setIsExistPoem] = useState(false);

  //메인페이지에 표출할 게시글 조회
  //이번달 작성 게시글 중 최다 북마크, 최다 조회수 게시글
  //이번달 작성된 게시글이 없을경우, 전체기간 최다 북마크, 최다 조회수 게시글 조회
  const getMainContent = async () => {
    try {
      const response = await axios.get("http://localhost:8080/mainContent");
      if (response.status === 200) {
        setPoem(response.data.poem);
        setPoemSettings(response.data.poemSettings);

        if (response.data.poemSettings.backgroundImage) {
          const src = `http://localhost:8080/poems/${response.data.poem.poemId}/backgroundImage/${response.data.poemSettings.backgroundImage}`;
          setBackgroundSrc(src);
        }
        setIsExistPoem(true);
        setLoadData(true);
      }
    } catch (error) {
      if (error.response.status === 404) {
        setIsExistPoem(false);
        setLoadData(true);
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };
  const { title, content, description, view, name } = poem;
  const { fontFamily, titleFontSize, contentFontSize, color, textAlign, backgroundOpacity } = poemSettings;

  //본문의 개행 적용
  const contentWithLineBreaks = content?.split("\n").map((line, index) => (
    <React.Fragment key={index}>
      {line}
      <br />
    </React.Fragment>
  ));

  const [backgroundSrc, setBackgroundSrc] = useState("");
  return (
    <Box display="flex" justifyContent="center">
      {loadData ? (
        <Box display="flex" flexDirection="column" alignItems="center" sx={{ width: 700, pt: 3 }}>
          <Typography variant="h3">POE-TRY</Typography>
          {isExistPoem && poem ? (
            <>
              <Box sx={{ mt: 3, display: "flex", flexDirection: "column", alignItems: "center" }}>
                <Typography variant="h5">이달의 시</Typography>
                <Divider sx={{ width: "100%" }} />
                <Typography variant="caption">조회수 {view}</Typography>
              </Box>

              <Box>
                <Box sx={{ display: "flex", mt: 3, alignItems: "center" }}>
                  <Typography variant="h6" sx={{ mb: 2, mr: 2 }}>
                    작성자
                  </Typography>
                  <UserAvatar memberId={poem.memberId} profileImage={poem.profileImage} name={poem.name} avatarSize={40} />
                  <Typography sx={{ mb: 2, ml: 2 }}>{name}</Typography>
                </Box>
              </Box>

              <Divider sx={{ width: "100%", mt: 3 }} />
              <Typography variant="h6" sx={{ mt: 2 }}>
                {description}
              </Typography>
              <Box
                component={Paper}
                sx={{
                  mt: 2,
                  height: 700,
                  width: 550,
                  wordBreak: "break-all",
                  backgroundSize: "cover",
                  backgroundPosition: "center",
                  backgroundRepeat: "no-repeat",
                  backgroundImage: `linear-gradient(rgba(255,255,255,${backgroundOpacity}), rgba(255,255,255,${backgroundOpacity})), url("${backgroundSrc}")`,
                }}
              >
                <Box component="div" sx={{ p: 5 }}>
                  <Typography variant={titleFontSize} fontFamily={fontFamily} color={color} textAlign={textAlign} sx={{ mt: 10 }}>
                    {title}
                  </Typography>
                  <br />

                  <Typography variant="body1" fontFamily={fontFamily} fontSize={contentFontSize} color={color} textAlign={textAlign} sx={{ mt: 3 }}>
                    {contentWithLineBreaks}
                  </Typography>
                  <br />
                  <br />
                  <br />
                </Box>
              </Box>
            </>
          ) : (
            <>
              <Box
                component={Paper}
                sx={{
                  mt: 5,
                  height: 700,
                  width: 550,
                }}
              >
                <Box component="div">
                  <Typography variant="h5" color="black" textAlign="center" sx={{ mt: 20 }}>
                    시 쓰기는 어렵지 않아요.
                  </Typography>
                  <Typography variant="h5" color="black" textAlign="center" sx={{ mt: 10 }}>
                    도전해볼까요?
                  </Typography>
                </Box>
              </Box>
            </>
          )}
        </Box>
      ) : null}
    </Box>
  );
};

export default MainPage;
