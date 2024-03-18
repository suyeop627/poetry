import React from "react";
import { AppBar, Toolbar, Button, Box, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { selectAuth, logout } from "../../auth/authSlice";
import axios from "axios";

//관리자 페이지 헤더
const AdminHeader = ({ setIsAdmin }) => {
  const { user } = useSelector(selectAuth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const handleLogout = async () => {
    try {
      const response = await axios.delete(`http://localhost:8080/auth/${user.refreshTokenId}`);
      if (response.status === 200) {
        dispatch(logout());
        alert("로그아웃됐습니다.");
        setIsAdmin(false);
        navigate("/");
        return;
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  const categories = ["poetry", "category", "members", "reports", "monitoring"];
  return (
    <AppBar position="static">
      <Toolbar style={{ justifyContent: "space-between" }}>
        <Box sx={{ display: "flex", justifyContent: "space-between" }}>
          <Button
            color="inherit"
            onClick={() => {
              navigate("/admin");
            }}
          >
            ADMIN
          </Button>
          {categories.map((category, index) => (
            <Button key={index} color="inherit" component={Link} to={`/admin/${category}`}>
              <Typography variant="body1">{category}</Typography>
            </Button>
          ))}
        </Box>
        <Box sx={{ display: "flex", alignItems: "center" }}>
          <Button color="inherit" onClick={handleLogout}>
            로그아웃
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default AdminHeader;
