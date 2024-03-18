import * as React from "react";
import Badge from "@mui/material/Badge";
import MailIcon from "@mui/icons-material/Mail";
import { useState } from "react";
import { Box, Button, Divider, IconButton, Menu, MenuItem, Typography } from "@mui/material";
import { useEffect } from "react";
import jwtAxios from "../../../auth/jwtAxios";
import { useNavigate } from "react-router-dom";
import NotificationStack from "./NotificationStack";
import FormattedDate from "../../../components/common/FormattedDate";
import EventsourceManager from "./EventsourceManager";

//알림 목록 표출
export default function NotificationList() {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = React.useState(null);
  const open = Boolean(anchorEl);
  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  const [newNotificationCount, setNewNotificationCount] = useState(0);
  const [notificationList, setNotificationList] = useState([]);
  const [newNotificationList, setNewNotificationList] = useState([]);
  useEffect(() => {
    getNotifications();
  }, []);

  //알림목록 컴포넌트 마운트 시, 알림목록 조회.
  //알림목록 컴포넌트 언마운트 시, 알림목록 모두 '읽음'처리 후 저장 요청
  useEffect(() => {
    if (open === true) {
      getNotifications();
    }
    if (open === false) {
      const tempNotifications = [...notificationList];
      setNewNotificationList([]);
      if (tempNotifications.length > 0) {
        const readNotifications = tempNotifications.map((noti) => ({ ...noti, read: true }));
        setNotificationList(readNotifications);
        setNeedToUpdateNotifications(true);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  //알림목록의 update요청 필요 여부를 판단하기 위한 state
  const [needToUpdateNotifications, setNeedToUpdateNotifications] = useState(false);

  useEffect(() => {
    if (needToUpdateNotifications) {
      updateNotifications();
      setNeedToUpdateNotifications(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [needToUpdateNotifications]);

  //확인하지 않는 알림 수 확인
  useEffect(() => {
    const unreadNotiCount = notificationList.filter((noti) => noti.read === false).length;
    setNewNotificationCount(unreadNotiCount);
  }, [notificationList]);

  //삭제할 알림 지정
  const handleNotifiactionDeletion = (index) => {
    const tempNotifications = [...notificationList];

    tempNotifications[index].deleted = true;
    setNotificationList(tempNotifications);
  };

  //알림 목록 조회 요청
  const getNotifications = async () => {
    try {
      const response = await jwtAxios.get("http://localhost:8080/notifications");
      if (response.status === 200) {
        setNotificationList(response.data);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  //알림 목록 수정 요청(알림 읽음 또는 삭제 처리)
  const updateNotifications = async () => {
    try {
      const response = await jwtAxios.put("http://localhost:8080/notifications", notificationList);
      if (response.status === 200) {
        setNeedToUpdateNotifications(false);
      }
    } catch (error) {
      alert("요청 처리 중 오류가 발생했습니다.");
    }
  };
  return (
    <div>
      <EventsourceManager setNotificationList={setNotificationList} setNewNotificationList={setNewNotificationList} />
      <IconButton
        aria-label="more"
        id="long-button"
        aria-controls={open ? "long-menu" : undefined}
        aria-expanded={open ? "true" : undefined}
        aria-haspopup="true"
        sx={{
          "&:hover": {
            cursor: "pointer",
          },
        }}
        onClick={handleClick}
      >
        <Badge badgeContent={newNotificationCount} color="error">
          <MailIcon htmlColor="#ffffff" />
        </Badge>
      </IconButton>
      <Menu
        id="long-menu"
        MenuListProps={{
          "aria-labelledby": "long-button",
        }}
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        sx={{ maxHeight: "60vh" }}
      >
        {notificationList.length > 0 ? (
          notificationList.map((notification, index) => (
            <Box key={"key" + index}>
              {!notification.deleted ? (
                <MenuItem key={index} sx={{ width: 450 }}>
                  <Box sx={{ display: "flex", width: "100%", flexDirection: "column" }}>
                    <Typography fontSize={"small"} color={notification.read ? "#A9A9A9" : "#000000"}>
                      {<FormattedDate localDateTime={notification.occurredAt} type={"time"} />}
                    </Typography>

                    <Typography fontSize={14} color={notification.read ? "#A9A9A9" : "#000000"} sx={{ whiteSpace: "pre-line" }}>
                      {notification.content}
                    </Typography>
                    <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                      {notification.notificationType === "BOOKMARK" ? (
                        <Button onClick={() => navigate(notification.toUrl, { state: { fromUrl: "/" } })}>담긴 시 보러가기</Button>
                      ) : null}
                      <Button color="error" onClick={() => handleNotifiactionDeletion(index)}>
                        알림 삭제
                      </Button>
                    </Box>
                    <Divider />
                  </Box>
                </MenuItem>
              ) : null}
            </Box>
          ))
        ) : (
          <MenuItem>알림이 없습니다.</MenuItem>
        )}
      </Menu>
      {newNotificationList.length > 0 ? (
        <NotificationStack notifications={newNotificationList} setNewNotificationList={setNewNotificationList} />
      ) : null}
    </div>
  );
}
