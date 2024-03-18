import React, { useEffect } from "react";

import { BrowserRouter, Route, Routes } from "react-router-dom";
import Login from "./pages/member/Login";
import SignUp from "./pages/member/SignUp";
import { useState } from "react";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import { Box } from "@mui/material";
import axios from "axios";
import MainPage from "./pages/MainPage";
import PoemIndex from "./pages/poem";
import AdminHeader from "./components/layout/AdminHeader";
import AdminIndex from "./pages/admin";
import FindPassword from "./pages/member/FindPassword";
import NotFoundPage from "./pages/components/NotFoundPage";

function App() {
  const [categories, setCategories] = useState([]);
  useEffect(() => {
    getCategories();
  }, []);
  const [isAdmin, setIsAdmin] = useState(false);

  const getCategories = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/category`);
      if (response.status === 200) {
        setCategories(response.data);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <BrowserRouter>
      {isAdmin ? <AdminHeader setIsAdmin={setIsAdmin} /> : <Header categories={categories} />}
      <Box style={{ minHeight: "70vh" }}>
        <Routes>
          <Route path="/signup" element={<SignUp />} />
          <Route path="/login" element={<Login />} />
          <Route path="/password" element={<FindPassword />} />
          <Route path="/" element={<MainPage />} />
          <Route path="/poems/*" element={<PoemIndex categories={categories} />} />
          <Route
            path="/admin/*"
            element={<AdminIndex setIsAdmin={setIsAdmin} isAdmin={isAdmin} categories={categories} setCategories={setCategories} />}
          />
          <Route path="/*" element={<NotFoundPage />} />
        </Routes>
      </Box>
      <Footer />
    </BrowserRouter>
  );
}

export default App;
