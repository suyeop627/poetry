import * as React from "react";
import { Box, Button, Typography } from "@mui/material";

//요청한 페이지가 없을 경우 표출
export default function NotFoundPage() {
  return (
    <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", flexDirection: "column", minHeight: "80vh" }}>
      <Typography variant="h1">404</Typography>
      <Typography variant="h6">페이지를 찾을 수 없습니다.</Typography>
      <Button>홈으로</Button>
    </Box>
  );
}
