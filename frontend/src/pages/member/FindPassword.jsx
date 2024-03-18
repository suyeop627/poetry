import * as React from "react";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import Typography from "@mui/material/Typography";
import Container from "@mui/material/Container";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

//비밀번호 찾기 페이지
export default function FindPassword() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    newPassword: "",
    confirmPassword: "",
  });
  const { email, newPassword, confirmPassword } = formData;

  //0-인증 전, 1-인증번호발송 2-인증완료
  const [emailCheck, setEmailCheck] = useState(0);
  const [verifyCode, setVerifyCode] = useState("");

  //이메일 인증번호 요청
  const verifyEmail = async () => {
    if (!validateEmail(email)) {
      alert("올바른 이메일을 입력하세요");
      return;
    }
    try {
      const response = await axios.post("http://localhost:8080/members/email/findPassword", email, {
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
      if (error.response.status === 404) {
        if (window.confirm("가입되지 않은 계정입니다.\n회원가입 페이지로 이동하시겠습니까?")) {
          navigate("/signup");
        }
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };

  //이메일 인증 이후, 이메일 재입력 버튼 클릭 시 기존 이메일 초기화
  const resetEmail = () => {
    setEmailCheck(0);
    setFormData({
      ...formData,
      email: "",
    });
  };

  //인증번호 유효성 검사 요청
  const checkVerifyCode = async () => {
    try {
      const verifyCodeObj = {
        email: email,
        code: verifyCode,
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
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };
  //이메일 유효성 검사
  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  //비밀번호 입력값 반영
  const handlePasswordChange = (event) => {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  //인증코드 입력값 반영
  const handleVerifyCode = (event) => {
    setVerifyCode(event.target.value);
  };

  //비밀번호 유효성 검사
  const validateNewPassword = () => {
    return newPassword.length >= 8 && newPassword.length <= 16 && newPassword === confirmPassword;
  };

  //비밀번호 변경 요청
  const changePassword = async () => {
    try {
      const response = await axios.put(`http://localhost:8080/members/password`, formData);
      if (response.status === 200) {
        if (window.confirm("비밀번호 변경이 완료됐습니다. 로그인 페이지로 이동하시겠습니까?")) {
          navigate("/login");
        } else {
          navigate("/");
        }
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
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
          <LockOutlinedIcon />
        </Avatar>
        <Typography component="h1" variant="h5">
          Find Account
        </Typography>
        <Box noValidate sx={{ mt: 1 }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="이메일"
            name="email"
            value={formData.email}
            autoFocus
            onChange={handlePasswordChange}
            disabled={emailCheck > 0 ? true : false}
          />

          {emailCheck === 0 ? (
            <Button onClick={verifyEmail} fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>
              인증번호 발송
            </Button>
          ) : emailCheck === 1 ? (
            <Grid item xs={12} sm={12}>
              <TextField required fullWidth id="verifyCode" label="인증번호" onChange={handleVerifyCode} />
              <Button fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} onClick={checkVerifyCode}>
                인증번호확인
              </Button>
              <Button fullWidth variant="contained" color="warning" sx={{ mb: 2 }} onClick={resetEmail}>
                이메일 재입력
              </Button>
            </Grid>
          ) : emailCheck === 2 ? (
            <>
              <TextField
                sx={{ mb: 1 }}
                required
                fullWidth
                name="newPassword"
                label="새 비밀번호(8~16자리)"
                type="password"
                id="newPassword"
                onChange={handlePasswordChange}
              />

              <TextField
                sx={{ mb: 1 }}
                required
                fullWidth
                label="비밀번호 확인(8~16자리)"
                type="password"
                name="confirmPassword"
                id="confirmPassword"
                onChange={handlePasswordChange}
              />

              <Button fullWidth variant="contained" onClick={() => changePassword()} disabled={!validateNewPassword()}>
                비밀번호 변경
              </Button>
            </>
          ) : null}
        </Box>
      </Box>
    </Container>
  );
}
