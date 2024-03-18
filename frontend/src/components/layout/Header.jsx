import React from "react";
import { AppBar, Toolbar, Button, Box, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { selectAuth, logout } from "../../auth/authSlice";
import MyPage from "../../pages/member/MyPage";
import axios from "axios";
import NotificationList from "../../pages/components/notification/NotificationList";

//헤더
const Header = ({ categories }) => {
  const { isLoggedIn, user } = useSelector(selectAuth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const handleLogout = async () => {
    try {
      const response = await axios.delete(`http://localhost:8080/auth/${user.refreshTokenId}`);
      if (response.status === 200) {
        dispatch(logout());
        alert("로그아웃됐습니다.");
        navigate("/");
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  return (
    <AppBar position="static">
      <Toolbar style={{ justifyContent: "space-between" }}>
        <Box sx={{ display: "flex", justifyContent: "space-between" }}>
          <Button
            color="inherit"
            onClick={() => {
              navigate("/");
            }}
          >
            POE_TRY
          </Button>
          {categories.map((category) => (
            <Button key={category.categoryId} color="inherit" component={Link} to={`/poems/category/${category.categoryId}`}>
              <Typography variant="body1">{category.categoryName}</Typography>
            </Button>
          ))}
        </Box>
        {isLoggedIn ? (
          <Box sx={{ display: "flex", alignItems: "center" }}>
            <Button color="inherit" component={Link} to="/poems/write">
              글쓰기
            </Button>
            <NotificationList />
            <MyPage name={user.name} />
            <Button color="inherit" onClick={handleLogout}>
              로그아웃
            </Button>
          </Box>
        ) : (
          <Box>
            <Button color="inherit" component={Link} to="/login">
              로그인
            </Button>
            <Button color="inherit" component={Link} to="/signup">
              회원가입
            </Button>
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;
