import * as React from "react";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import Link from "@mui/material/Link";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import Container from "@mui/material/Container";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { login, selectAuth } from "../../auth/authSlice";
import { useEffect } from "react";
import { Login } from "@mui/icons-material";

//로그인페이지
export default function SignIn() {
  const [formData, setFormData] = React.useState({
    email: "",
    password: "",
    confirmPassword: "",
    name: "",
    phone: "",
    gender: "",
  });
  const { isLoggedIn } = useSelector(selectAuth);

  //로그인 된 상테에서 접근시 홈으로 이동
  useEffect(() => {
    if (isLoggedIn) {
      navigate("/");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const dispatch = useDispatch();
  const navigate = useNavigate();

  //로그인 정보 입력값 반영
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
  const validatePassword = (password) => {
    return password.length >= 8 && password.length <= 16;
  };

  //로그인 제한일이 존재할 경우, LocalDateTime 형식의 날짜를 yyyy년 mm월 dd일 형식으로 변경
  const getFormattedDate = (localDateTime) => {
    const originalDate = new Date(localDateTime);
    const year = originalDate.getFullYear();
    const month = ("0" + (originalDate.getMonth() + 1)).slice(-2);
    const date = ("0" + originalDate.getDate()).slice(-2);

    return `${year}년 ${month}월 ${date}일`;
  };

  //로그인 요청
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!(validateEmail(formData.email) && validatePassword(formData.password))) {
      alert("올바른 이메일 및 비밀번호를 입력하세요.");
      return;
    }
    try {
      const response = await axios.post("http://localhost:8080/auth", formData);
      if (response.status === 200) {
        const { memberId, name, refreshTokenId, accessToken, roles, restrictionEndDate } = response.data;

        if (restrictionEndDate) {
          let deleteAccount = window.confirm(
            `${name}님께서는 부적절한 활동이 확인되어, 일정기간 활동이 제한됩니다. \n\n제한 종료 일자 : ${getFormattedDate(
              restrictionEndDate
            )}\n\n탈퇴하시겠습니까?`
          );

          if (deleteAccount) {
            deleteRestrictedMember(memberId);
          }
        } else {
          dispatch(login({ memberId, name, accessToken, refreshTokenId, roles }));
          alert("로그인됐습니다.");
          navigate("/");
        }
      }
    } catch (error) {
      if (error.response.status && error.response.status === 401) {
        alert("이메일 또는 비밀번호를 다시 확인해주세요");
        return;
      } else if (error.response.status === 403 && error.response.data) {
        const { email, restrictionEndDate } = error.response.data;
        alert(`입력하신 이메일 \n${email}\n은 ${getFormattedDate(restrictionEndDate)} 까지 사용할 수 없는 계정입니다.`);
        return;
      }
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //로그인 제한된 회원이 탈퇴를 선택할 경우, 해당 회원 탈퇴처리
  const deleteRestrictedMember = async (memberId) => {
    try {
      const response = await axios.delete(`http://localhost:8080/members/${memberId}/restricted`);
      if (response.status === 200) {
        alert("탈퇴가 완료됐습니다.");
        navigate("/");
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
          <Login />
        </Avatar>
        <Typography variant="h4">POE-TRY</Typography>
        <Typography variant="h5" sx={{ mt: 2 }}>
          로그인
        </Typography>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField margin="normal" required fullWidth id="email" label="이메일" name="email" autoFocus onChange={handleChange} />
          <TextField margin="normal" required fullWidth name="password" label="비밀번호" type="password" id="password" onChange={handleChange} />
          <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>
            Login
          </Button>
          <Grid container>
            <Grid item xs>
              <Link href="/password" variant="body2">
                비밀번호를 잊으셨나요?
              </Link>
            </Grid>
            <Grid item>
              <Link href="/signup" variant="body2">
                {"계정이 없으신가요?"}
              </Link>
            </Grid>
          </Grid>
        </Box>
      </Box>
    </Container>
  );
}
