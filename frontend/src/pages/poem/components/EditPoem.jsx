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
import { useNavigate, useParams } from "react-router-dom";
import jwtAxios from "../../../auth/jwtAxios";
import { useSelector } from "react-redux";
import { selectAuth } from "../../../auth/authSlice";

//게시글 수정 페이지
const EditPoem = ({ categories }) => {
  const { poemId } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, user } = useSelector(selectAuth);
  const [poemSettings, setPoemSettings] = useState({
    titleFontSize: "",
    contentFontSize: "",
    fontFamily: "",
    color: "",
    textAlign: "",
    backgroundImage: "",
    backgroundOpacity: "",
  });
  const [poem, setPoem] = useState({
    categoryId: "",
    title: "",
    content: "",
    description: "",
  });
  const [loadData, setLoadData] = useState(false);
  //현재 이미지 주소
  const [backgroundSrc, setBackgroundSrc] = useState("");

  //로그인하지 않은 경우 접근 불가 및 뒤로가기
  useEffect(() => {
    if (!isLoggedIn) {
      alert("권한이 없습니다.");
      navigate(-1);
      return;
    }
    getPoemDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  //게시글 및 설정  정보 조회
  const getPoemDetails = async () => {
    try {
      setLoadData(false);
      let response;
      response = await jwtAxios.get(`http://localhost:8080/poems/${poemId}`);

      if (response.status === 200) {
        if (response.data.poem.memberId !== user.memberId) {
          alert("권한이 없습니다.");
          navigate(-1);
          return;
        }
        setPoem(response.data.poem);
        setPoemSettings(response.data.poemSettings);
        setLoadData(true);
        if (response.data.poemSettings.backgroundImage) {
          const src = `http://localhost:8080/poems/${poemId}/backgroundImage/${response.data.poemSettings.backgroundImage}`;
          setBackgroundSrc(src);
        }
      }
    } catch (error) {
      if (error.response.status === 404) {
        alert("존재하지 않는 게시글입니다.");
        navigate(-1);
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };

  //게시글 수정 입력값 반영
  const handleChange = (event) => {
    const { name, value } = event.target;
    setPoem({
      ...poem,
      [name]: value,
    });
  };
  //게시글 설정 수정 입력값 반영
  const handlePoemSettingChange = (event) => {
    const { name, value } = event.target;
    setPoemSettings({
      ...poemSettings,
      [name]: value,
    });
  };
  //렌더링할 이미지 바이트
  const [backgroundImage, setBackgroundImage] = useState("");

  const [fileForm, setFileForm] = useState("");

  //이미지 파일 업로드시 변경사항 반영
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
    }
  };
  //이미지 삭제시 관련 값 삭제
  const clearImg = () => {
    setPoemSettings({ ...poemSettings, backgroundImage: "" });
    setBackgroundImage("");
    setBackgroundSrc("");
    setFileForm("");
  };

  const [isFormValidated, setIsFormValidated] = useState(false);

  //모든 유효성 검사 통과 시 수정버튼 활성화
  useEffect(() => {
    if (poem.categoryId && poem.title && poem.content && poem.description) {
      setIsFormValidated(true);
    } else {
      setIsFormValidated(false);
    }
  }, [poem.categoryId, poem.content, poem.description, poem.title]);

  useEffect(() => {
    validateContent(poem.content);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [poem.content]);

  //본문 내용 유효성 검사(글자수 500이하 && 줄 수 20 이하)
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
  //게시글 수정 요청
  const handleSubmit = async (event) => {
    let form = new FormData();
    event.preventDefault();
    form.append("poem", new Blob([JSON.stringify(poem)], { type: "application/json" }));
    form.append("poemSettings", new Blob([JSON.stringify(poemSettings)], { type: "application/json" }));
    form.append("backgroundImage", fileForm);
    try {
      const response = await jwtAxios.put(`http://localhost:8080/poems/${poemId}`, form, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      if (response.status === 200) {
        alert("작성하신 내용이 저장됐습니다.");

        navigate(`/poems/${poemId}`, { state: { fromUrl: `/poems/category/${poem.categoryId}` } });
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <Container component="main" sx={{ width: "100%" }}>
      <Box
        sx={{
          marginTop: 8,
          alignItems: "center",
        }}
      >
        <Typography component="h1" variant="h5">
          수정 페이지
        </Typography>
        {loadData ? (
          <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <FormControl fullWidth sx={{ mt: 2, mb: 2 }}>
                  <InputLabel size="small" sx={{ backgroundColor: "white" }}>
                    카테고리
                  </InputLabel>
                  <Select
                    size="small"
                    labelId="category"
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

                <TextField required fullWidth id="title" label="제목" value={poem.title} name="title" autoFocus onChange={handleChange} />
                <TextField
                  required
                  fullWidth
                  multiline
                  id="content"
                  label="내용"
                  name="content"
                  value={poem.content}
                  rows={5}
                  onChange={handleChange}
                  sx={{ mt: 2, maxHeight: "200px", whiteSpace: "break-spaces" }}
                  inputProps={{ style: { fontSize: 13 } }}
                />
                <Typography variant="caption" sx={{ whiteSpace: "pre-line" }}>
                  500, 20줄 이내로 작성해주세요.(글자수: {poem.content.length}, 줄 수: {poem.content.split("\n").length})
                </Typography>
                <TextField
                  required
                  fullWidth
                  id="description"
                  label="설명"
                  name="description"
                  value={poem.description}
                  onChange={handleChange}
                  sx={{ mt: 2 }}
                  inputProps={{ maxLength: 100 }}
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
                    <MenuItem value="serif">serif</MenuItem>
                    <MenuItem value="sans-serif">sans-serif</MenuItem>
                    <MenuItem value="맑은고딕">맑은 고딕</MenuItem>
                    <MenuItem value="Dotum">돋움</MenuItem>
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
                        value={poemSettings.contentFontSize}
                        onChange={handlePoemSettingChange}
                        inputProps={{
                          name: "contentFontSize",
                          id: "content-font-size",
                        }}
                        size="small"
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
                        value={poemSettings.color}
                        onChange={handlePoemSettingChange}
                        inputProps={{
                          name: "color",
                          id: "color",
                        }}
                        size="small"
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
                      <InputLabel size="small" sx={{ backgroundColor: "white" }}>
                        정렬
                      </InputLabel>
                      <Select
                        value={poemSettings.textAlign}
                        onChange={handlePoemSettingChange}
                        inputProps={{
                          name: "textAlign",
                          id: "text-align",
                        }}
                        size="small"
                      >
                        <MenuItem value="left">좌측 정렬</MenuItem>
                        <MenuItem value="center">가운데 정렬</MenuItem>
                        <MenuItem value="right">우측 정렬</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                </Grid>
                {!backgroundImage && !backgroundSrc ? (
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
                    valueLabelDisplay="auto"
                    step={0.1}
                    min={0}
                    max={1}
                  />
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <PoemPreview poem={poem} poemSettings={poemSettings} backgroundImage={backgroundImage ? backgroundImage : backgroundSrc} />
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
        ) : null}
      </Box>
    </Container>
  );
};

export default EditPoem;
