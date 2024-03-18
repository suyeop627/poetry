import React from "react";
import { TextField, FormControl, Select, MenuItem, Grid, Box, Typography, InputLabel } from "@mui/material";

//ReportManagement의 검색조건 설정 컴포넌트
const ReportSearch = ({ reportSearchParameter, handleChange }) => {
  return (
    <Box sx={{ ml: 5, mb: 2 }}>
      <Typography variant="h6" sx={{ mb: 3 }}>
        검색 조건
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={4}>
          <TextField
            name="title"
            label="시 제목"
            value={reportSearchParameter.title}
            fullWidth
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            size="small"
          />
        </Grid>
        <Grid item xs={4}>
          <TextField
            name="content"
            label="시 내용"
            value={reportSearchParameter.content}
            fullWidth
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            size="small"
          />
        </Grid>{" "}
        <Grid item xs={4}>
          <TextField
            name="writer"
            label="작성자"
            inputProps={{ maxLength: 16 }}
            value={reportSearchParameter.writer}
            fullWidth
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            size="small"
          />
        </Grid>
        <Grid item xs={4}>
          {/* <TextField name="reportStatus" label="처리 상태" value={reportSearchParameter.reportStatus} fullWidthonChange={handleChange} size="small" /> */}
          <FormControl fullWidth size="small">
            <InputLabel id="search-type-label">처리상태</InputLabel>
            <Select
              labelId="search-type-label"
              id="search-type"
              value={reportSearchParameter.reportStatus}
              onChange={(e) => handleChange("reportStatus", e.target.value)}
              fullWidth
              label="처리상태"
              size="small"
            >
              <MenuItem value="REPORTED">신고접수</MenuItem>
              <MenuItem value="UNDER_REVIEW">검토중</MenuItem>
              <MenuItem value="DONE">처리완료</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={4}>
          <TextField
            name="name"
            label="관리자 이름"
            value={reportSearchParameter.name}
            fullWidth
            inputProps={{ maxLength: 16 }}
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            size="small"
          />
        </Grid>
        <Grid item xs={4}>
          <TextField
            name="email"
            label="관리자 메일"
            value={reportSearchParameter.email}
            fullWidth
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            size="small"
          />
        </Grid>
        <Grid item xs={4}>
          <TextField
            name="startDate"
            label="시작일"
            type="date"
            value={reportSearchParameter.startDate}
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            InputLabelProps={{
              shrink: true,
            }}
            fullWidth
            size="small"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            name="endDate"
            label="종료일"
            type="date"
            value={reportSearchParameter.endDate}
            onChange={(event) => handleChange(event.target.name, event.target.value)}
            InputLabelProps={{
              shrink: true,
            }}
            fullWidth
            size="small"
          />
        </Grid>
        <Grid item xs={4} />
      </Grid>
    </Box>
  );
};

export default ReportSearch;
