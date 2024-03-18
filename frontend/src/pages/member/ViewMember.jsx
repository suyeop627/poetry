import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";

import { Divider, Grid, List, ListItem, ListItemIcon, ListItemText, Paper } from "@mui/material";
import { AccountCircle, Email, Man, Phone, Woman } from "@mui/icons-material";
import jwtAxios from "../../auth/jwtAxios";
import { useDispatch, useSelector } from "react-redux";
import { logout, selectAuth } from "../../auth/authSlice";
import { useNavigate } from "react-router-dom";
import UserAvatar from "../../components/common/UserAvatar";

//회원 정보 조회 페이지
export default function ViewMember({ toggleDrawer, pageToggle, setPageToggle, memberInfo }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector(selectAuth);

  //회원 탈퇴 요청
  const deleteMember = async () => {
    try {
      if (
        !window.confirm("탈퇴하시겠습니까?\n\n*관리자에 의해 이용제한일이 설정된 경우, \n이용 제한일 종료까지 해당 계정으로 재가입이 불가능합니다. ")
      ) {
        return;
      }
      //탈퇴하려는 회원이 관리자일경우, 최종 신고 처리자 값 비워둠을 알림
      if (user.roles.includes("ROLE_ADMIN")) {
        const countOfDoneByMember = await countIfExistDonByMember(user.memberId);
        if (countOfDoneByMember > 0) {
          const setDoneByNull = window.confirm(
            `관리자님께서 최종 수정하신 신고내역 ${countOfDoneByMember} 건 이 존재합니다. 
            \n탈퇴 시, 최종 처리자 정보도 함께 삭제됩니다.
            \n탈퇴하시겠습니까?`
          );
          if (!setDoneByNull) {
            return;
          }
        }
      }
      //회원 탈퇴 요청
      const response = await jwtAxios.delete(`http://localhost:8080/members/${memberInfo.memberId}`);
      if (response.status === 200) {
        alert("회원 탈퇴가 완료됐습니다.");
        dispatch(logout());
        navigate("/");
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  //탈퇴하려는 회원이 관리자일경우, 신고 처리내역 중 최종처리한 개수 조회 요청
  const countIfExistDonByMember = async (memberId) => {
    try {
      const response = await jwtAxios.get(`http://localhost:8080/reports/members/${memberId}`);
      if (response.status === 200) {
        return response.data;
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  return (
    <Box
      sx={{
        width: 500,
        padding: 2,
        paddingTop: 10,
      }}
    >
      <Box
        component={Paper}
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "space-evenly",
          minHeight: 700,
        }}
      >
        <UserAvatar memberId={memberInfo.memberId} profileImage={memberInfo.profileImage} name={memberInfo.name} avatarSize={120} />
        <Divider style={{ width: "80%" }} />
        <List>
          <ListItem>
            <ListItemIcon>
              <Email />
            </ListItemIcon>
            <ListItemText secondary="이메일" primary={memberInfo.email} />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <AccountCircle />
            </ListItemIcon>
            <ListItemText secondary="이름" primary={memberInfo.name} />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <Phone />
            </ListItemIcon>
            <ListItemText secondary="연락처" primary={memberInfo.phone} />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <Woman />
              <Man />
            </ListItemIcon>
            <ListItemText secondary="성별" primary={memberInfo.gender} />
          </ListItem>
        </List>
        <Button sx={{ mt: 5 }} fullWidth color="info" onClick={() => navigate(`/poems/members/${memberInfo.memberId}/${memberInfo.name}/0`)}>
          나의 POE-TRY 보러가기
        </Button>
      </Box>

      <Grid container spacing={2} sx={{ mt: 3 }}>
        <Grid item xs={6}>
          <Button fullWidth variant="outlined" color="info" onClick={() => setPageToggle("edit")}>
            정보수정
          </Button>
        </Grid>
        <Grid item xs={6}>
          <Button fullWidth variant="outlined" color="error" onClick={() => deleteMember()}>
            회원탈퇴
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
}
