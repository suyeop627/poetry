import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { BookmarksOutlined } from "@mui/icons-material";
import UserAvatar from "../../../components/common/UserAvatar";
import { Divider } from "@mui/material";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 300,
  bgcolor: "background.paper",
  border: "2px solid #000",
  boxShadow: 24,
  p: 4,
};

//게시글을 북마크한 회원 목록 표출 모달
export default function BookmarkModal({ bookmarkMemberList }) {
  const [bookmarkMemberListOpen, setBookmarkMemberListOpen] = React.useState(false);
  const handleBookmakrMemberListOpen = () => setBookmarkMemberListOpen(true);
  const handleBookmakrMemberListClose = () => setBookmarkMemberListOpen(false);

  return (
    <div>
      <Button onClick={handleBookmakrMemberListOpen}>
        <BookmarksOutlined />
        {bookmarkMemberList?.length}
      </Button>
      <Modal
        open={bookmarkMemberListOpen}
        onClose={handleBookmakrMemberListClose}
        aria-labelledby="modal-modal-title"
        aria-describedby="modal-modal-description"
      >
        <Box sx={style}>
          <Typography variant="h6">이 시를 담아간 회원</Typography>
          <Divider sx={{ mb: 3 }} />

          {bookmarkMemberList && bookmarkMemberList.length > 0 ? (
            <Box sx={{ maxHeight: "20vh", overflowY: "auto" }}>
              {bookmarkMemberList.map((member, index) => (
                <div key={member.memberId}>
                  <Box sx={{ display: "flex", justifyContent: "space-evenly" }}>
                    <UserAvatar memberId={member.memberId} profileImage={member.profileImage} name={member.name} avatarSize={50} />
                    <Typography sx={{ mt: 1 }}>{member.name}</Typography>
                  </Box>
                  <Divider sx={{ mb: 1, width: "90%" }} />
                </div>
              ))}
            </Box>
          ) : (
            <Typography>북마크한 회원이 없습니다.</Typography>
          )}
        </Box>
      </Modal>
    </div>
  );
}
