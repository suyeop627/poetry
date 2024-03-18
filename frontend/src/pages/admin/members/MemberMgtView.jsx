import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";

import { Avatar, Checkbox, Container, FormControl, FormControlLabel, Grid, InputLabel, MenuItem, Select, TextField, Typography } from "@mui/material";
import { useState } from "react";
import jwtAxios from "../../../auth/jwtAxios";
import { useEffect } from "react";
import { useSelector } from "react-redux";
import { selectAuth } from "../../../auth/authSlice";
import UserAvatar from "../../../components/common/UserAvatar";

//회원 정보 조회 및 수정
export default function MemberMgtView({ setOpen, memberInfo, getMemberPage, pageParameter }) {
  //회원 정보 저장 state
  const [formData, setFormData] = useState({
    memberId: memberInfo?.memberId,
    email: memberInfo?.email,
    name: memberInfo?.name,
    phone: memberInfo?.phone,
    gender: memberInfo?.gender,
    password: "",
    profileImage: memberInfo?.profileImage,
    roles: memberInfo?.roles,
  });

  //입력값 반영
  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };
  const { user } = useSelector(selectAuth);

  //이름 유효성 검사
  const validateName = (name) => {
    return name.length >= 2 && name.length <= 16;
  };

  const [isFormValidated, setIsFormValidated] = useState(false);

  //유효성 검사 통과시 수정버튼 활성화
  useEffect(() => {
    if (validateName(formData.name) && formData.gender) {
      setIsFormValidated(true);
    } else {
      setIsFormValidated(false);
    }
  }, [formData.gender, formData.name, formData.password, formData.phone, isFormValidated]);

  //이미지 파일 저장 state
  const [fileForm, setFileForm] = useState("");

  //정보 수정 요청
  const handleSubmit = async (event) => {
    if (memberInfo.memberId === user.memberId) {
      alert("본인정보 수정은 마이페이지를 이동해주세요.");
      return;
    }
    let form = new FormData();
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
        setFileForm("");
        getMemberPage(pageParameter);
        alert("정보 수정이 완료됐습니다.");
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  const [userImage, setUserImage] = useState("");

  //프로필 사진 업로드 처리
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

  //이미지 삭제시 처리
  const clearImg = () => {
    setFormData({ ...formData, profileImage: "" });
    setUserImage("");
    setFileForm("");
  };

  //관리자 체크박스 처리
  const handleAdminCheckBox = (e) => {
    const isChecked = e.target.checked;
    if (isChecked) {
      setFormData({ ...formData, roles: [...formData.roles, "ROLE_ADMIN"] });
    } else {
      setFormData({ ...formData, roles: formData.roles.filter((role) => role !== "ROLE_ADMIN") });
    }
  };

  //회원 삭제요청 처리
  const handleDeleteMember = async () => {
    if (window.confirm("해당 회원을 삭제하시겠습니까?")) {
      if (memberInfo.memberId === user.memberId) {
        alert("본인정보 삭제은 마이페이지를 이동해주세요.");
        return;
      }
      try {
        const response = await jwtAxios.delete(`http://localhost:8080/members/${memberInfo.memberId}`);
        if (response.status === 200) {
          alert("삭제되었습니다.");

          setFileForm("");
          getMemberPage(pageParameter);
          setOpen(false);
        }
      } catch (error) {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
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
          <UserAvatar memberId={memberInfo?.memberId} profileImage={memberInfo?.profileImage} name={memberInfo?.name} avatarSize={120} />
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
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="name"
                  label="이름(2~16자)"
                  name="name"
                  onChange={handleChange}
                  value={formData.name}
                  inputProps={{ maxLength: 16 }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="phone"
                  inputProps={{ maxLength: 13 }}
                  label="휴대전화"
                  name="phone"
                  disabled={true}
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
              <Grid item xs={12}>
                <FormControlLabel
                  control={<Checkbox checked={formData.roles?.includes("ROLE_ADMIN")} onChange={handleAdminCheckBox} name="isAdmin" />}
                  label="관리자 설정"
                />
              </Grid>
            </Grid>
            <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} disabled={!isFormValidated}>
              정보수정
            </Button>
            <Button fullWidth variant="contained" color={"error"} onClick={handleDeleteMember}>
              삭제
            </Button>
          </Box>
        </Box>
      </Container>
    </Box>
  );
}
