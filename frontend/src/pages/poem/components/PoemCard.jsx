import * as React from "react";

import Card from "@mui/material/Card";
import CardHeader from "@mui/material/CardHeader";
import CardMedia from "@mui/material/CardMedia";
import CardContent from "@mui/material/CardContent";
import Typography from "@mui/material/Typography";
import { useNavigate } from "react-router-dom";
import UserAvatar from "../../../components/common/UserAvatar";
import { Box, Divider, Grid } from "@mui/material";
import FormattedDate from "../../../components/common/FormattedDate";
import { BookmarksOutlined, SearchOutlined } from "@mui/icons-material";
import CategoryNameFromId from "../../../components/common/CategoryNameFromId";

//게시글 목록의 각 게시글 요소
export default function PoemCard({ poem, pageParameter, fromUrl, categories }) {
  //본문길이 50자 이상일 경우, 50자까지만 표출
  const contentFiftyLength = poem.content?.length > 50 ? poem.content.substring(0, 50) + " ... " : poem.content;

  //설명길이 20자 이상일 경우, 20자까지만 표출
  const descriptionFortyLength = poem.description?.length > 20 ? poem.description.substring(0, 20) + " ... " : poem.description;
  const navigate = useNavigate();

  return (
    <Card sx={{ mb: 3, display: "flex" }}>
      <Grid container>
        <Grid item xs={3}>
          <CardHeader
            avatar={<UserAvatar memberId={poem.memberId} profileImage={poem.profileImage} name={poem.name} />}
            title={poem.name}
            subheader={<FormattedDate localDateTime={poem.writeDate} />}
          />
          <Typography variant="body2" color="text.secondary" sx={{ ml: 3, mb: 2 }}>
            {descriptionFortyLength}
          </Typography>
        </Grid>

        <Grid item xs={9}>
          <Box
            onClick={() => navigate(`/poems/${poem.poemId}`, { state: { pageParameter: pageParameter, fromUrl: fromUrl } })}
            sx={{
              p: 3,
              "&:hover": {
                cursor: "pointer",
              },
            }}
          >
            <CardMedia sx={{ ml: 5 }}>
              <CategoryNameFromId categories={categories} id={poem.categoryId} />

              <Typography>{poem.title}</Typography>
              <Divider />
            </CardMedia>

            <CardContent>
              <Typography sx={{ ml: 5 }}>{contentFiftyLength}</Typography>
            </CardContent>
            <Typography sx={{ float: "right" }} fontSize={"small"} color={Text.secondary}>
              <SearchOutlined /> {poem.view} <BookmarksOutlined /> {poem.bookmarkMemberList.length}
            </Typography>
          </Box>
        </Grid>
      </Grid>
    </Card>
  );
}
