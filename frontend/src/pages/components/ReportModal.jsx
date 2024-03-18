import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Modal from "@mui/material/Modal";
import { useState } from "react";
import { Checkbox, Divider, FormControlLabel, TextField } from "@mui/material";
import { useEffect } from "react";
import jwtAxios from "../../auth/jwtAxios";

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 400,
  bgcolor: "background.paper",
  border: "2px solid #000",
  boxShadow: 24,
  p: 4,
};

//게시글 신고 사유 입력 모달
export default function ReportModal({ reportModalOpen, handleReportModalClose, poemId, checkIsReportedAlready }) {
  useEffect(() => {
    if (!reportModalOpen) {
      setReportComment("");
      setReportReasonType({
        POEM_CONTENT: false,
        POEM_BACKGROUND_IMAGE: false,
        POEM_IRRELEVANT_CATEGORY: false,
        MEMBER_PROFILE_IMAGE: false,
        MEMBER_NAME: false,
      });
    }
  }, [reportModalOpen]);

  //신고 사유 선택 유효성 검사
  const validateCheckbox = () => {
    return Object.values(reportReasonType).includes(true);
  };
  //신고의 구체적 사유 유효성 검사
  const validateReportComment = () => {
    return reportComment.length >= 5;
  };

  //신고 저장 요청
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validateCheckbox() || !validateReportComment()) {
      alert("하나이상의 신고 사유를 선택해주시고,\n구체적인 신고사유를 5자 이상 작성해주세요.");
      return;
    }

    // 선택된 항목들의 값을 배열에 저장하여 서버로 전송
    const selectedType = Object.keys(reportReasonType).filter((item) => reportReasonType[item]);
    const reportObj = {
      poemId: poemId,
      reportReasonTypes: selectedType,
      reportComment: reportComment,
    };
    try {
      const response = await jwtAxios.post(`http://localhost:8080/reports`, reportObj);
      if (response.status === 201) {
        alert("신고가 완료됐습니다.");
        handleReportModalClose();
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };

  const [reportReasonType, setReportReasonType] = useState({
    POEM_CONTENT: false,
    POEM_BACKGROUND_IMAGE: false,
    POEM_IRRELEVANT_CATEGORY: false,
    MEMBER_PROFILE_IMAGE: false,
    MEMBER_NAME: false,
  });
  const [reportComment, setReportComment] = useState("");

  const handleCheckboxChange = (event) => {
    setReportReasonType({
      ...reportReasonType,
      [event.target.name]: event.target.checked,
    });
  };

  const handleReportCommentChange = (event) => {
    setReportComment(event.target.value);
  };
  return (
    <div>
      <Button onClick={checkIsReportedAlready} color="error" variant="contained">
        신고하기
      </Button>
      <Modal open={reportModalOpen} onClose={handleReportModalClose} aria-labelledby="modal-modal-title" aria-describedby="modal-modal-description">
        <Box sx={style}>
          <Typography id="modal-modal-title" variant="h6" component="h2" align="center">
            신고하기
          </Typography>

          <Divider />

          <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
            <Typography id="modal-modal-description" sx={{ mt: 2 }}>
              신고사유<Typography variant="caption">(복수선택가능)</Typography>
            </Typography>
            <Divider />
            <FormControlLabel
              control={<Checkbox checked={reportReasonType.item1} onChange={handleCheckboxChange} name="POEM_CONTENT" />}
              label="불건전한 내용 포함"
            />
            <FormControlLabel
              control={<Checkbox checked={reportReasonType.item2} onChange={handleCheckboxChange} name="POEM_BACKGROUND_IMAGE" />}
              label="불건전한 배경 이미지 사용"
            />
            <FormControlLabel
              control={<Checkbox checked={reportReasonType.item3} onChange={handleCheckboxChange} name="POEM_IRRELEVANT_CATEGORY" />}
              label="카테고리와 관련 없는 내용"
            />
            <FormControlLabel
              control={<Checkbox checked={reportReasonType.item4} onChange={handleCheckboxChange} name="MEMBER_PROFILE_IMAGE" />}
              label="불건전한 회원 프로필 이미지"
            />
            <FormControlLabel
              control={<Checkbox checked={reportReasonType.item5} onChange={handleCheckboxChange} name="MEMBER_NAME" />}
              label="불건전한 회원 이름"
            />
            <Divider />
            <TextField
              multiline
              label="구체적인 신고 사유"
              value={reportComment}
              onChange={handleReportCommentChange}
              fullWidth
              rows={3}
              sx={{ mb: 2, mt: 2 }}
              inputProps={{ maxLength: 30 }}
            />
            <Divider />
            <Box sx={{ display: "flex", justifyContent: "space-around", mt: 3 }}>
              <Button variant="contained" onClick={handleReportModalClose}>
                취소
              </Button>
              <Button variant="contained" color="error" type="submit">
                신고하기
              </Button>
            </Box>
          </Box>
        </Box>
      </Modal>
    </div>
  );
}
