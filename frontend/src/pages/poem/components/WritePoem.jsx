import React, { useCallback, useEffect, useState } from "react";
import {
  Box,
  Button,
  Container,
  FormControl,
  FormControlLabel,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Slider,
  TextField,
  Typography,
} from "@mui/material";
import PoemPreview from "./PoemPreview";
import { useNavigate } from "react-router-dom";
import jwtAxios from "../../../auth/jwtAxios";

//게시글 작성 페이지
const WritePoem = ({ categories }) => {
  const navigate = useNavigate();
  const [poemSettings, setPoemSettings] = useState({
    titleFontSize: "h5",
    contentFontSize: "medium",
    fontFamily: "serif",
    color: "black",
    textAlign: "left",
    backgroundImage: "",
    backgroundOpacity: 0,
  });
  const [poem, setPoem] = useState({
    categoryId: "",
    title: "",
    content: "",
    description: "",
  });

  //게시글 작성 입력값 반영
  const handleChange = (event) => {
    const { name, value } = event.target;
    setPoem({
      ...poem,
      [name]: value,
    });
  };
  //게시글 설정 입력값 반영
  const handlePoemSettingChange = (event) => {
    const { name, value } = event.target;
    setPoemSettings({
      ...poemSettings,
      [name]: value,
    });
  };
  const [backgroundImage, setBackgroundImage] = useState("");

  const [fileForm, setFileForm] = useState("");

  //배경이미지 업로드 시 입력값 처리
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setFileForm(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setBackgroundImage(reader.result);
      };
      reader.readAsDataURL(file);
    } else {
      setFileForm("");
      setBackgroundImage("");
    }
  };

  //배경이미지 삭제 시, 관련 값 초기화
  const clearImg = () => {
    setPoemSettings({ ...poemSettings, backgroundImage: "" });
    setBackgroundImage("");
    setFileForm("");
  };

  const [isFormValidated, setIsFormValidated] = useState(false);

  //모든 유효성 검사 성공 시, 저장버튼 활성화
  useEffect(() => {
    if (poem.categoryId && poem.title && poem.content && poem.description) {
      setIsFormValidated(true);
    } else {
      setIsFormValidated(false);
    }
  }, [poem.categoryId, poem.content, poem.description, poem.title]);

  //글 저장 요청
  const handleSubmit = async (event) => {
    let form = new FormData();
    event.preventDefault();
    form.append("poem", new Blob([JSON.stringify(poem)], { type: "application/json" }));
    form.append("poemSettings", new Blob([JSON.stringify(poemSettings)], { type: "application/json" }));
    form.append("backgroundImage", fileForm);
    try {
      const response = await jwtAxios.post("http://localhost:8080/poems", form, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      if (response.status === 201) {
        const url = new URL(response.headers.location);
        const path = url.pathname;
        alert("작성하신 내용이 저장됐습니다.");
        navigate(path, { state: { fromUrl: `/poems/category/${poem.categoryId}` } });
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  //게시글 본문 유효성 확인
  useEffect(() => {
    validateContent(poem.content);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [poem.content]);

  //게시글 본문 유효성 확인(본문길이 500자 이하, 총 줄 수 20줄 이하)
  const validateContent = useCallback(
    (content) => {
      if (content && (content.length > 500 || content.split("\n").length > 20)) {
        let contentSliced;
        alert("글자수 제한을 초과했습니다.");
        if (content.length > 500) {
          contentSliced = content.substring(0, 500);
        } else if (content.split("\n").length > 20) {
          contentSliced = content.substring(0, content.lastIndexOf("\n"));
        }
        setPoem({
          ...poem,
          content: contentSliced,
        });
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [poem.content]
  );

  return (
    <Container component="main" sx={{ width: "100%" }}>
      <Box
        sx={{
          marginTop: 8,
          alignItems: "center",
        }}
      >
        <Typography component="h1" variant="h5">
          작성 페이지
        </Typography>
        <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <FormControl fullWidth sx={{ mt: 2, mb: 2 }}>
                <InputLabel sx={{ backgroundColor: "white" }} size="small">
                  카테고리
                </InputLabel>
                <Select
                  size="small"
                  value={poem.categoryId}
                  onChange={handleChange}
                  inputProps={{
                    name: "categoryId",
                    id: "categoryId",
                  }}
                >
                  {categories.map((category) => (
                    <MenuItem key={category.categoryId} value={category.categoryId}>
                      {category.categoryName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <TextField
                required
                fullWidth
                id="title"
                label="제목(50자이내)"
                name="title"
                autoFocus
                onChange={handleChange}
                inputProps={{ maxLength: 50 }}
              />
              <TextField
                required
                fullWidth
                multiline
                id="content"
                label="내용(500자, 20줄 이내)"
                name="content"
                rows={5}
                value={poem.content}
                onChange={handleChange}
                sx={{ mt: 2, maxHeight: "200px", whiteSpace: "break-spaces" }}
                inputProps={{ style: { fontSize: 13 }, maxLength: 500 }}
              />
              <Typography variant="caption" sx={{ whiteSpace: "pre-line" }}>
                *500자, 20줄 이내로 작성해주세요.(글자수: {poem.content.length}, 줄 수: {poem.content.split("\n").length})
              </Typography>
              <TextField
                required
                fullWidth
                id="description"
                label="설명(50자이내)"
                name="description"
                onChange={handleChange}
                sx={{ mt: 2 }}
                inputProps={{ maxLength: 50 }}
              />
              <FormControl fullWidth sx={{ mt: 2 }}>
                <InputLabel size="small" sx={{ backgroundColor: "white" }}>
                  글꼴
                </InputLabel>
                <Select
                  value={poemSettings.fontFamily}
                  onChange={handlePoemSettingChange}
                  inputProps={{
                    name: "fontFamily",
                    id: "font-family",
                  }}
                  size="small"
                >
                  <MenuItem value="맑은고딕">맑은 고딕</MenuItem>
                  <MenuItem value="Dotum">돋움</MenuItem>
                  <MenuItem value="serif">serif</MenuItem>
                  <MenuItem value="sans-serif">sans-serif</MenuItem>
                  <MenuItem value="궁서체">궁서체</MenuItem>
                </Select>
              </FormControl>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <FormControl fullWidth sx={{ mt: 2 }}>
                    <InputLabel size="small" sx={{ backgroundColor: "white" }}>
                      제목 크기
                    </InputLabel>
                    <Select
                      value={poemSettings.titleFontSize}
                      onChange={handlePoemSettingChange}
                      inputProps={{
                        name: "titleFontSize",
                        id: "title-font-size",
                      }}
                      size="small"
                    >
                      <MenuItem value="h6">작게</MenuItem>
                      <MenuItem value="h5">보통</MenuItem>
                      <MenuItem value="h4">크게</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={6}>
                  <FormControl fullWidth sx={{ mt: 2 }}>
                    <InputLabel size="small" sx={{ backgroundColor: "white" }}>
                      본문 크기
                    </InputLabel>
                    <Select
                      size="small"
                      value={poemSettings.contentFontSize}
                      onChange={handlePoemSettingChange}
                      inputProps={{
                        name: "contentFontSize",
                        id: "content-font-size",
                      }}
                    >
                      <MenuItem value="small">작게</MenuItem>
                      <MenuItem value="medium">보통</MenuItem>
                      <MenuItem value="large">크게</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <FormControl fullWidth sx={{ mt: 2 }}>
                    <InputLabel size="small" sx={{ backgroundColor: "white" }}>
                      색상
                    </InputLabel>
                    <Select
                      size="small"
                      value={poemSettings.color}
                      onChange={handlePoemSettingChange}
                      inputProps={{
                        name: "color",
                        id: "color",
                      }}
                    >
                      <MenuItem value="red">Red</MenuItem>
                      <MenuItem value="blue">Blue</MenuItem>
                      <MenuItem value="green">Green</MenuItem>
                      <MenuItem value="black">Black</MenuItem>
                      <MenuItem value="White">White</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={6}>
                  <FormControl fullWidth sx={{ mt: 2 }}>
                    <InputLabel sx={{ backgroundColor: "white" }} size="small">
                      정렬
                    </InputLabel>
                    <Select
                      size="small"
                      value={poemSettings.textAlign}
                      onChange={handlePoemSettingChange}
                      inputProps={{
                        name: "textAlign",
                        id: "text-align",
                      }}
                    >
                      <MenuItem value="left">좌측 정렬</MenuItem>
                      <MenuItem value="center">가운데 정렬</MenuItem>
                      <MenuItem value="right">우측 정렬</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
              {!backgroundImage ? (
                <Button variant="outlined" sx={{ mt: 2 }} fullWidth>
                  <FormControlLabel
                    control={
                      <TextField
                        onChange={handleFileChange}
                        type="file"
                        label="사진"
                        variant="standard"
                        style={{ display: "none" }}
                        inputProps={{ accept: "image/*" }}
                      />
                    }
                    label="배경 이미지 등록하기"
                  />
                </Button>
              ) : (
                <Button sx={{ mt: 2 }} fullWidth color="error" variant="outlined" onClick={() => clearImg()}>
                  배경 이미지 삭제
                </Button>
              )}

              <Typography sx={{ mt: 2, color: "gray" }}>배경 투명도</Typography>
              <FormControl fullWidth>
                <Slider
                  value={poemSettings.backgroundOpacity}
                  onChange={(e, value) => handlePoemSettingChange(e, value)}
                  name="backgroundOpacity"
                  aria-labelledby="background-opacity"
                  step={0.1}
                  min={0}
                  max={1}
                />
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <PoemPreview poem={poem} poemSettings={poemSettings} backgroundImage={backgroundImage} />
            </Grid>
          </Grid>
          <Box sx={{ display: "flex", justifyContent: "space-evenly" }}>
            <Button sx={{ width: 200 }} color="error" variant="contained" onClick={() => navigate(-1)}>
              취소
            </Button>
            <Button sx={{ width: 200 }} type="submit" variant="contained" disabled={!isFormValidated}>
              저장하기
            </Button>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};

export default WritePoem;
