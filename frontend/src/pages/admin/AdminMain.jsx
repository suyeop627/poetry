import * as React from "react";
import { Box, Typography } from "@mui/material";

//관리자 메인페이지
export default function AdminMain() {
  return (
    <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", flexDirection: "column", minHeight: "80vh" }}>
      <Typography variant="h2">관리자 페이지</Typography>
    </Box>
  );
}
