import React from "react";
import { Box, Paper, Typography } from "@mui/material";

//게시글 미리보기 컴포넌트(글작성 또는 글 수정시)
const PoemPreview = ({ poem, poemSettings, backgroundImage }) => {
  const { title, content, description } = poem;
  const { fontFamily, titleFontSize, contentFontSize, color, textAlign, backgroundOpacity } = poemSettings;

  //본문의 개행 적용
  const contentWithLineBreaks = content.split("\n").map((line, index) => (
    <React.Fragment key={index}>
      {line}
      <br />
    </React.Fragment>
  ));
  return (
    <Box>
      <Box
        component={Paper}
        sx={{
          minHeight: 700,
          width: 550,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          backgroundImage: `linear-gradient(rgba(255,255,255,${backgroundOpacity}), rgba(255,255,255,${backgroundOpacity})), url("${backgroundImage}")`,
          wordBreak: "break-all",
        }}
      >
        <Box component="div" sx={{ p: 5 }}>
          <Typography variant={titleFontSize} fontFamily={fontFamily} color={color} textAlign={textAlign} sx={{ mt: 10 }}>
            {title}
          </Typography>
          <br />

          <Typography variant="body1" fontFamily={fontFamily} fontSize={contentFontSize} color={color} textAlign={textAlign} sx={{ mt: 1 }}>
            {contentWithLineBreaks}
          </Typography>
          <br />
          <br />
          <br />
        </Box>
      </Box>
      <Typography variant="caption">설명: {description}</Typography>
    </Box>
  );
};

export default PoemPreview;
