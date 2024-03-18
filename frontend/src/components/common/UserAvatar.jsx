import React, { useEffect } from "react";
import { Avatar, Box } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

//사용자 프로필 아바타
const UserAvatar = ({ memberId, profileImage, name, avatarSize }) => {
  const navigate = useNavigate();
  const src = memberId && profileImage ? `http://localhost:8080/members/${memberId}/profileImage/${profileImage}` : "";
  const [size, setSize] = useState(64);
  useEffect(() => {
    if (avatarSize) {
      setSize(avatarSize);
    }
  }, [avatarSize]);
  return (
    <Box>
      <Avatar
        sx={{
          width: size,
          height: size,
          mb: 2,
          "&:hover": {
            cursor: "pointer",
          },
        }}
        alt="User Avatar"
        src={src}
        onClick={() => navigate(`/poems/members/${memberId}/${name}/0`)}
      />
    </Box>
  );
};

export default UserAvatar;
