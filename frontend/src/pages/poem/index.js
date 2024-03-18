import { Box } from "@mui/material";
import React from "react";

import { Route, Routes } from "react-router-dom";

import PoemList from "./components/PoemList";
import WritePoem from "./components/WritePoem";
import EditPoem from "./components/EditPoem";
import ViewPoem from "./components/ViewPoem";
import MemberPoemList from "./components/MemberPoemList";
import NotFoundPage from "../components/NotFoundPage";

const PoemIndex = ({ categories }) => {
  return (
    <Box display="flex" justifyContent="center">
      <Routes>
        <Route path="/category/:categoryId" element={<PoemList categories={categories} />} />
        <Route path="/members/:memberId/:name/:tabNumber" element={<MemberPoemList categories={categories} />} />
        <Route path="/write" element={<WritePoem categories={categories} />} />
        <Route path="/:poemId/edit" element={<EditPoem categories={categories} />} />
        <Route path="/:poemId" element={<ViewPoem categories={categories} />} />
        <Route path="/*" element={<NotFoundPage />} />
      </Routes>
    </Box>
  );
};

export default PoemIndex;
