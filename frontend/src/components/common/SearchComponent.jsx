import React, { useEffect, useState } from "react";
import { TextField, FormControl, Select, MenuItem, Button, Grid } from "@mui/material";

//회원조회/게시글 조회 페이지의 검색 컴포넌트
const SearchComponent = ({ handleChange, pageParameter, doSearch, target }) => {
  const [searchTarget, setSearchTarget] = useState("");

  useEffect(() => {
    setSearchTarget(target);
  }, [target]);

  const getMenuItemForTarget = () => {
    if (searchTarget === "poem") {
      return [
        <MenuItem key="title" value="title">
          제목
        </MenuItem>,
        <MenuItem key="writer" value="writer">
          작성자
        </MenuItem>,
        <MenuItem key="content" value="content">
          내용
        </MenuItem>,
      ];
    } else if (searchTarget === "member") {
      return [
        <MenuItem key="name" value="name">
          이름
        </MenuItem>,
        <MenuItem key="email" value="email">
          메일
        </MenuItem>,
        <MenuItem key="phone" value="phone">
          전화번호
        </MenuItem>,
        <MenuItem key="role" value="role">
          역할
        </MenuItem>,
      ];
    }
  };

  useEffect(() => {
    if (pageParameter.type === "role") {
      if (pageParameter.keyword !== "ROLE_USER" || pageParameter.keyword !== "ROLE_ADMIN") {
        handleChange("keyword", "ROLE_USER");
      }
    } else {
      handleChange("keyword", "");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageParameter.type]);
  return (
    <Grid container spacing={1} alignItems="center">
      <Grid item xs={3}>
        <FormControl fullWidth>
          <Select
            labelId="search-type-label"
            id="search-type"
            value={searchTarget ? pageParameter.type : ""}
            onChange={(e) => handleChange("type", e.target.value)}
            fullWidth
            size="small"
          >
            {searchTarget ? getMenuItemForTarget(searchTarget) : <MenuItem key="default" value=""></MenuItem>}
          </Select>
        </FormControl>
      </Grid>
      <Grid item xs={6}>
        {pageParameter.type === "role" ? (
          <Select
            labelId="search-type-label"
            id="search-type"
            value={pageParameter.keyword}
            onChange={(e) => handleChange("keyword", e.target.value)}
            fullWidth
            size="small"
          >
            <MenuItem key="admin" value="ROLE_ADMIN">
              관리자
            </MenuItem>
            <MenuItem key="user" value="ROLE_USER">
              일반회원
            </MenuItem>
          </Select>
        ) : (
          <TextField
            id="search-keyword"
            label="검색어"
            variant="outlined"
            size="small"
            value={pageParameter.keyword}
            onChange={(e) => handleChange("keyword", e.target.value)}
          />
        )}
      </Grid>
      <Grid item xs={3}>
        <Button size="small" variant="contained" color="primary" onClick={() => doSearch(pageParameter)} sx={{ height: 40 }}>
          검색
        </Button>
      </Grid>
    </Grid>
  );
};

export default SearchComponent;
