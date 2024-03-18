import * as React from "react";
import Drawer from "@mui/material/Drawer";
import Button from "@mui/material/Button";
import ViewMember from "./ViewMember";
import jwtAxios from "../../auth/jwtAxios";
import { useEffect } from "react";
import { useSelector } from "react-redux";
import { selectAuth } from "../../auth/authSlice";
import { useCallback } from "react";
import { useState } from "react";
import EditMember from "./EditMember";

//회원 마이페이지(pageToggle에 따라, 조회 또는 수정 컴포넌트 랜더링)
export default function MyPage({ name }) {
  const [open, setOpen] = useState(false);
  const [pageToggle, setPageToggle] = useState("view");
  const { user } = useSelector(selectAuth);
  const toggleDrawer = (open) => (event) => {
    if (event.type === "keydown" && (event.key === "Tab" || event.key === "Shift")) {
      return;
    }
    setPageToggle("view");
    setOpen(open);
  };
  const [memberInfo, setMemberInfo] = useState({});
  const [loadData, setLoadData] = useState(false);

  //회원 정보 요청
  const getMemberInfo = useCallback(async () => {
    try {
      const memberId = user.memberId;
      const response = await jwtAxios.get(`http://localhost:8080/members/${memberId}`);
      if (response.status === 200) {
        setMemberInfo(response.data);
        setLoadData(true);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  }, [setLoadData, user.memberId]);

  useEffect(() => {
    if (open) {
      getMemberInfo();
    }
  }, [getMemberInfo, open]);
  return (
    <div>
      <Button color="inherit" onClick={toggleDrawer(true)}>
        {`${name}'s page`}
      </Button>
      {loadData ? (
        <Drawer anchor="right" open={open} onClose={toggleDrawer(false)}>
          {pageToggle === "view" ? (
            <ViewMember toggleDrawer={toggleDrawer} pageToggle={pageToggle} setPageToggle={setPageToggle} memberInfo={memberInfo} />
          ) : (
            <EditMember
              toggleDrawer={toggleDrawer}
              pageToggle={pageToggle}
              setPageToggle={setPageToggle}
              memberInfo={memberInfo}
              setLoadData={setLoadData}
              getMemberInfo={getMemberInfo}
            />
          )}
        </Drawer>
      ) : null}
    </div>
  );
}
