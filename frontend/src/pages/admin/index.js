import { Box } from "@mui/material";
import React, { useEffect } from "react";

import { Route, Routes } from "react-router-dom";
import CategoryManagement from "./category/CategoryManagement";
import MemberManagement from ".//members/MemberManagement";

import AdminMain from "./AdminMain";

import { useSelector } from "react-redux";
import { selectAuth } from "../../auth/authSlice";
import jwtAxios from "../../auth/jwtAxios";
import ReportManagement from "./report/ReportManagement";
import ReportDetailsView from "./report/ReportDetailsView";
import PoetryInfo from "./poetryInfo/PoetryInfo";
import NotFoundPage from "../components/NotFoundPage";
import Monitoring from "./monitoring/Monitoring";

//관리자 인증 비밀번호 확인 및 라우팅
const AdminIndex = ({ setIsAdmin, isAdmin, categories, setCategories }) => {
  const { isLoggedIn, user } = useSelector(selectAuth);

  //비로그인 또는 일반회원 접근 불가
  useEffect(() => {
    if (!isLoggedIn || !user.roles.includes("ROLE_ADMIN")) {
      alert("잘못된 접근입니다.");
      window.location.href = "/";
    } else {
      checkIsAdmin();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  //관리자 비밀번호 일치여부 확인 요청
  const checkIsAdmin = async () => {
    const password = prompt("관리자 비밀번호를 입력하세요");
    if (password) {
      try {
        const response = await jwtAxios.post("http://localhost:8080/admin/validatePassword", password, {
          headers: {
            "Content-Type": "text/plain",
          },
        });
        if (response.status === 200) {
          setIsAdmin(true);
          alert("인증이 완료됐습니다.");
        }
      } catch (error) {
        if (error.response.status === 403) {
          alert("인증에 실패했습니다.");
          window.location.href = "/";
        } else {
          alert("요청 처리 중 오류가 발생했습니다.");
        }
      }
    } else {
      alert("인증에 실패했습니다.");
      window.location.href = "/";
    }
  };
  return (
    <Box display="flex" justifyContent="center">
      {isAdmin ? (
        <Routes>
          <Route path="/" element={<AdminMain />} />
          <Route path="/category" element={<CategoryManagement categories={categories} setCategories={setCategories} />} />
          <Route path="/poetry" element={<PoetryInfo />} />
          <Route path="/members" element={<MemberManagement />} />
          <Route path="/reports" element={<ReportManagement />} />
          <Route path="/reports/:reportId" element={<ReportDetailsView categories={categories} />} />
          <Route path="/monitoring" element={<Monitoring />} />
          <Route path="/*" element={<NotFoundPage />} />
        </Routes>
      ) : null}
    </Box>
  );
};

export default AdminIndex;
