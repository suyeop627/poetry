import React, { useCallback, useEffect, useState } from "react";
import axios from "axios";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import Container from "@mui/material/Container";
import Link from "@mui/material/Link";
import { useNavigate } from "react-router-dom";
import { selectAuth } from "../../auth/authSlice";
import { LoginRounded } from "@mui/icons-material";
import { useSelector } from "react-redux";

//회원 가입 페이지
export default function SignUp() {
  //회원 가입 입력 정보 저장
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    confirmPassword: "",
    name: "",
    phone: "",
    gender: "",
  });
  const navigate = useNavigate();
  //로그인 한 상태에서 접근 시 홈으로 이동
  useEffect(() => {
    if (isLoggedIn) {
      navigate("/");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const { isLoggedIn } = useSelector(selectAuth);

  //회원 정보 입력값 반영
  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  //이메일 유효성 검사
  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  //비밀번호 유효성 검사
  const validatePassword = useCallback((password, confirmPassword) => {
    return password === confirmPassword && password.length >= 8 && password.length <= 16;
  }, []);

  //휴대전화 유효성 검사
  const validatePhoneNumber = (phone) => {
    const phoneRegex = /^(01[016789])\d{3,4}\d{4}$/;
    return phoneRegex.test(phone);
  };
  //이름 유효성 검사
  const validateName = (name) => {
    return name.length >= 2 && name.length <= 16;
  };

  //이메일 인증 단계 저장, 0-인증 전, 1-인증번호발송 2-인증완료
  const [emailCheck, setEmailCheck] = useState(0);
  const [verifyCode, setVerifyCode] = useState("");

  //이메일 인증코드 입력값 저장
  const handleVerifyCode = (event) => {
    setVerifyCode(event.target.value);
  };

  //로그인 제한일이 존재할 경우, LocalDateTime 형식의 날짜를 yyyy년 mm월 dd일 형식으로 변경
  const getFormattedDate = (localDateTime) => {
    const originalDate = new Date(localDateTime);
    const year = originalDate.getFullYear();
    const month = ("0" + (originalDate.getMonth() + 1)).slice(-2);
    const date = ("0" + originalDate.getDate()).slice(-2);

    return `${year}년 ${month}월 ${date}일`;
  };

  //이메일 인증코드 발송 요청
  const verifyEmail = async () => {
    const email = formData.email;
    if (!validateEmail(email)) {
      alert("올바른 이메일 형식을 입력해주세요.");
      return;
    }
    try {
      const response = await axios.post("http://localhost:8080/members/email/signIn", email, {
        headers: {
          "Content-Type": "text/plain",
        },
      });
      if (response.status === 200) {
        setEmailCheck(1);
        alert("입력한 이메일로 인증번호가 발송됐습니다. 확인 후, 인증번호를 입력해주세요.");
      } else {
        alert("다시시도해주세요");
        setEmailCheck(0);
      }
    } catch (error) {
      if (error.response.status === 403 && error.response.data) {
        const { email, restrictionEndDate } = error.response.data;
        alert(`입력하신 이메일 \n${email}\n은 ${getFormattedDate(restrictionEndDate)} 까지 사용할 수 없는 계정입니다.`);
        return;
      } else if (error.response.status === 409) {
        alert("이미 사용중인 이메일입니다.");
      } else {
        alert("인증번호 발송에 실패했습니다. 이메일 확인 후 다시 시도해주세요.");
        setEmailCheck(0);
      }
    }
  };

  //이메일 인증코드 확인 요청
  const checkVerifyCode = async () => {
    try {
      const verifyCodeObj = {
        email: formData.email,
        verifyCode: verifyCode,
      };
      const response = await axios.post("http://localhost:8080/members/verifyCode", verifyCodeObj);
      if (response.status === 200) {
        setEmailCheck(2);
        alert("이메일 인증이 완료됐습니다.");
      } else {
        alert("다시 시도해주세요");
        setEmailCheck(0);
      }
    } catch (error) {
      if (error.response.status === 400) {
        alert("인증코드를 잘못입력했습니다.");
        console.log(error.response);
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };

  //이메일 및 이메일 인증단계 초기화
  const resetEmail = () => {
    setEmailCheck(0);
    setFormData({
      ...formData,
      email: "",
    });
  };
  const [isFormValidated, setIsFormValidated] = useState(false);

  //모든 유효성 검사 통과시 가입버튼 활성화
  useEffect(() => {
    if (
      validateEmail(formData.email) &&
      validatePassword(formData.password, formData.confirmPassword) &&
      validateName(formData.name) &&
      validatePhoneNumber(formData.phone) &&
      formData.gender &&
      emailCheck === 2
    ) {
      setIsFormValidated(true);
    } else {
      setIsFormValidated(false);
    }
  }, [
    emailCheck,
    formData.confirmPassword,
    formData.email,
    formData.gender,
    formData.name,
    formData.password,
    formData.phone,
    isFormValidated,
    validatePassword,
  ]);

  //회원가입 요청
  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      const response = await axios.post("http://localhost:8080/members", formData);
      if (response.status === 201) {
        if (window.confirm("회원가입이 완료됐습니다. 로그인페이지로 이동하시겠습니까?")) {
          navigate("/login");
        } else {
          navigate("/");
        }
      }
    } catch (error) {
      if (error.response.status === 409) {
        alert("입력하신 휴대전화 번호는 이미 사용중인 번호입니다.");
      }
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: "secondary.main" }}>
          <LoginRounded />
        </Avatar>
        <Typography variant="h4">POE-TRY</Typography>
        <Typography variant="h5" sx={{ mt: 2 }}>
          회원가입
        </Typography>
        <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={12}>
              <TextField
                required
                fullWidth
                id="email"
                label="이메일"
                name="email"
                autoFocus
                value={formData.email}
                onChange={handleChange}
                disabled={emailCheck === 0 ? false : true}
              />
            </Grid>
            {emailCheck === 0 ? ( //인증요청 전
              <Grid item xs={12} sm={12}>
                <Button fullWidth variant="contained" sx={{ mt: 2, mb: 2 }} onClick={verifyEmail}>
                  이메일 인증
                </Button>
              </Grid>
            ) : emailCheck === 1 ? ( //인증 요청후 코드 입력창
              <Grid item xs={12} sm={12}>
                <TextField required fullWidth id="verifyCode" label="인증번호" onChange={handleVerifyCode} inputProps={{ maxLength: 6 }} />
                <Button fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} onClick={checkVerifyCode}>
                  인증번호확인
                </Button>
                <Button fullWidth variant="contained" color="warning" sx={{ mb: 2 }} onClick={resetEmail}>
                  이메일 재입력
                </Button>
              </Grid>
            ) : null}
            <Grid item xs={12} sm={12}>
              <TextField
                required
                fullWidth
                name="password"
                label="비밀번호(8~16자리)"
                type="password"
                id="password"
                onChange={handleChange}
                inputProps={{ maxLength: 16 }}
              />
            </Grid>
            <Grid item xs={12} sm={12}>
              <TextField
                required
                fullWidth
                label="비밀번호 확인(8~16자리)"
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                inputProps={{ maxLength: 16 }}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField required fullWidth id="name" label="이름(2~16자)" name="name" onChange={handleChange} inputProps={{ maxLength: 16 }} />
            </Grid>
            <Grid item xs={12}>
              <TextField required fullWidth id="phone" label="휴대전화" name="phone" onChange={handleChange} inputProps={{ maxLength: 12 }} />
            </Grid>
            <Grid item xs={12}>
              <Box sx={{ minWidth: 120 }}>
                <FormControl fullWidth>
                  <InputLabel id="demo-simple-select-label">성별</InputLabel>
                  <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={formData.gender}
                    label="Gender"
                    onChange={handleChange}
                    name="gender"
                  >
                    <MenuItem value={"FEMALE"}>FEMALE</MenuItem>
                    <MenuItem value={"MALE"}>MALE</MenuItem>
                  </Select>
                </FormControl>
              </Box>
            </Grid>
          </Grid>
          <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} disabled={!isFormValidated}>
            회원가입
          </Button>
          <Grid container justifyContent="flex-end">
            <Grid item>
              <Link href="/login" variant="body2">
                이미 계정이 있으신가요?
              </Link>
            </Grid>
          </Grid>
        </Box>
      </Box>
    </Container>
  );
}
