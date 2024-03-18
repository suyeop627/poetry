import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";

import { Avatar, Container, FormControl, FormControlLabel, Grid, InputLabel, MenuItem, Select, TextField, Typography } from "@mui/material";
import { useState } from "react";
import jwtAxios from "../../auth/jwtAxios";
import { useEffect } from "react";
import { useCallback } from "react";
import { useDispatch } from "react-redux";
import { login } from "../../auth/authSlice";
import UserAvatar from "../../components/common/UserAvatar";

//회원 정보 수정 페이지
export default function EditMember({ setPageToggle, memberInfo, getMemberInfo }) {
  const dispatch = useDispatch();
  //회원 정보 저장 및 수정 요청시 사용
  const [formData, setFormData] = useState({
    memberId: memberInfo.memberId,
    email: memberInfo.email,
    name: memberInfo.name,
    phone: memberInfo.phone,
    gender: memberInfo.gender,
    password: "",
    profileImage: memberInfo.profileImage,
    roles: memberInfo.roles,
  });
  //비밀번호 변경 요청시 활용
  const [passwordObj, setPasswordObj] = useState({
    email: memberInfo.email,
    password: "",
    newPassword: "",
    confirmPassword: "",
  });
  //비밀번호 변경 컴포넌트 랜더링 판단
  const [isPasswordChage, setIsPasswordChange] = useState(false);

  //비밀번호 변경시 입력값 반영
  const handlePasswordChange = (event) => {
    const { id, value } = event.target;
    setPasswordObj({
      ...passwordObj,
      [id]: value,
    });
  };

  //비밀번호 유효성 검사
  const validatePassword = useCallback((password) => {
    return password.length >= 8 && password.length <= 16;
  }, []);
  useEffect(() => {
    setPasswordObj({
      ...passwordObj,
      password: formData.password,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [formData.password]);

  //새 비밀번호 작성시 유효성 검사
  const validateNewPassword = () => {
    return (
      formData.password.length >= 8 &&
      formData.password.length <= 16 &&
      passwordObj.newPassword.length >= 8 &&
      passwordObj.newPassword.length <= 16 &&
      passwordObj.newPassword === passwordObj.confirmPassword
    );
  };

  //비밀번호 변경 요청
  const changePassword = async () => {
    try {
      const response = await jwtAxios.put(`http://localhost:8080/members/${memberInfo.memberId}/password`, passwordObj);
      if (response.status === 200) {
        alert("비밀번호 변경이 완료됐습니다.");
        setIsPasswordChange(false);
        setFormData({ ...formData, password: "" });
      }
    } catch (error) {
      if (error.response && error.response.status === 400) {
        alert("현재 비밀번호를 잘못입력하셨습니다.");
        return;
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };

  //회원 정보 수정사항 반영
  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  //전화번호 유효성 검사
  const validatePhoneNumber = (phone) => {
    const phoneRegex = /^(01[016789])\d{3,4}\d{4}$/;
    return phoneRegex.test(phone);
  };
  //회원 이름 유효성 검사
  const validateName = (name) => {
    return name.length >= 2 && name.length <= 16;
  };

  const [isFormValidated, setIsFormValidated] = useState(false);

  //각 필드 유효성 검사 통과 여부에따라 수정버튼 활성화
  useEffect(() => {
    if (validateName(formData.name) && validatePhoneNumber(formData.phone) && formData.gender && validatePassword(formData.password)) {
      setIsFormValidated(true);
    } else {
      setIsFormValidated(false);
    }
  }, [formData.gender, formData.name, formData.password, formData.phone, isFormValidated, validatePassword]);

  let form = new FormData();
  const [fileForm, setFileForm] = useState("");

  //정보 수정 요청
  const handleSubmit = async (event) => {
    event.preventDefault();
    form.append("updateInfo", new Blob([JSON.stringify(formData)], { type: "application/json" }));
    form.append("profileImage", fileForm);

    try {
      const response = await jwtAxios.put(`http://localhost:8080/members/${memberInfo.memberId}`, form, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      if (response.status === 200) {
        const { memberId, name, refreshTokenId, accessToken, roles } = response.data;
        dispatch(login({ memberId, name, accessToken, refreshTokenId, roles }));
        setPageToggle("view");
        getMemberInfo();
        alert("정보 수정이 완료됐습니다.");
      }
    } catch (error) {
      if (error.response.data && error.response.status === 400) {
        alert("현재 비밀번호를 잘못입력하셨습니다.");
        return;
      } else if (error.response.status === 409) {
        alert("입력하신 휴대전화번호는 이미 사용중인 번호입니다.");
        return;
      }

      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //프로필 이미지 입력 시 저장할 state
  const [userImage, setUserImage] = useState("");

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setFileForm(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setUserImage(reader.result);
      };
      reader.readAsDataURL(file);
    } else {
      setFileForm("");
      setUserImage("");
    }
  };
  //이미지 삭제 버튼 클릭시 관련 값 초기화
  const clearImg = () => {
    setFormData({ ...formData, profileImage: "" });
    setUserImage("");
    setFileForm("");
  };
  return (
    <Box
      sx={{
        width: 500,
        padding: 2,
        paddingTop: 10,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
      }}
      role="presentation"
    >
      <Typography component="h1" variant="h5">
        정보 수정
      </Typography>

      {!userImage ? (
        formData.profileImage ? (
          <UserAvatar memberId={memberInfo.memberId} profileImage={memberInfo.profileImage} name={memberInfo.name} avatarSize={120} />
        ) : (
          <Avatar sx={{ width: 120, height: 120, mb: 2 }} alt="User Avatar" src={userImage ? userImage : ""} />
        )
      ) : (
        <Avatar sx={{ width: 120, height: 120, mb: 2 }} alt="User Avatar" src={userImage ? userImage : ""} />
      )}
      {formData.profileImage || userImage ? (
        <Button color="error" variant="outlined" onClick={() => clearImg()}>
          프로필이미지 삭제
        </Button>
      ) : (
        <Button variant="outlined" sx={{ pl: 5 }}>
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
            label="프로필 사진 등록하기"
          />
        </Button>
      )}

      <Container component="main" maxWidth="xs">
        <Box
          sx={{
            marginTop: 8,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
          }}
        >
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
                  onChange={handleChange}
                  disabled={true}
                  value={formData.email}
                />
              </Grid>
              <Grid item xs={12} sm={12}>
                <TextField
                  required
                  fullWidth
                  name="password"
                  label="현재 비밀번호(8~16자)"
                  type="password"
                  id="password"
                  inputProps={{ maxLength: 16 }}
                  value={formData.password}
                  onChange={handleChange}
                />
              </Grid>
              {isPasswordChage ? (
                <>
                  <Grid item xs={12} sm={12}>
                    <TextField
                      required
                      fullWidth
                      name="newPassword"
                      label="새 비밀번호(8~16자)"
                      type="password"
                      id="newPassword"
                      inputProps={{ maxLength: 16 }}
                      onChange={handlePasswordChange}
                    />
                  </Grid>
                  <Grid item xs={12} sm={12}>
                    <TextField
                      required
                      fullWidth
                      label="비밀번호 확인(8~16자)"
                      type="password"
                      id="confirmPassword"
                      inputProps={{ maxLength: 16 }}
                      onChange={handlePasswordChange}
                    />
                  </Grid>
                  <Grid item xs={12} sm={12}>
                    <Button fullWidth variant="contained" onClick={() => changePassword()} disabled={!validateNewPassword()}>
                      비밀번호 변경
                    </Button>
                  </Grid>
                </>
              ) : null}
              <Grid item xs={12}>
                <Button variant="outlined" fullWidth onClick={() => setIsPasswordChange(!isPasswordChage)}>
                  {!isPasswordChage ? "비밀번호 변경하기" : "비밀번호 변경 취소"}
                </Button>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="name"
                  label="이름(2~16자)"
                  inputProps={{ maxLength: 16 }}
                  name="name"
                  onChange={handleChange}
                  value={formData.name}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="phone"
                  label="휴대전화"
                  inputProps={{ maxLength: 13 }}
                  name="phone"
                  onChange={handleChange}
                  value={formData.phone}
                />
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
              정보수정
            </Button>
            <Grid>
              <Grid item>
                <Button variant="outlined" fullWidth onClick={() => setPageToggle("view")}>
                  마이페이지
                </Button>
              </Grid>
            </Grid>
          </Box>
        </Box>
      </Container>
    </Box>
  );
}
