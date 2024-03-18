import * as React from "react";
import Pagination from "@mui/material/Pagination";
import Stack from "@mui/material/Stack";

//목록페이지 페이지네이션 컴포넌트
export default function BasicPagination({ page, handleChange, pageParameter }) {
  return (
    <Stack spacing={2}>
      <Pagination
        sx={{ display: "flex", justifyContent: "center" }}
        count={pageParameter.totalPages}
        color="primary"
        page={page}
        onChange={(e, value) => {
          handleChange("page", value);
        }}
      />
    </Stack>
  );
}
