import { CloseOutlined } from "@mui/icons-material";
import { Alert, AlertTitle, Button, Collapse, IconButton, Typography } from "@mui/material";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

//실시간 알림 메시지창 표출
export default function NotificationAlert({ notification, newNotificationList, setNewNotificationList }) {
  const [open, setOpen] = useState(true);
  const navigate = useNavigate();
  const handleToUrl = (toUrl) => {
    setOpen(false);
    navigate(toUrl);
  };
  //표출된 실시간 알림 메시지창 닫기
  const deleteNewNotification = (notificationIdToDelete) => {
    const notifications = newNotificationList.filter((noti) => noti.notificationId !== notificationIdToDelete);
    setOpen(false);
    setNewNotificationList(notifications);
  };
  //알림 내용을 50자까지만 표출
  const notificationFiftyLength = notification.content?.length > 50 ? notification.content.substring(0, 50) + " ... " : notification.content;

  //알림 메시지창 3초간 유지 후 닫기
  useEffect(() => {
    setTimeout(() => {
      deleteNewNotification(notification.notificationId);
    }, 1000 * 5);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <Collapse in={open} unmountOnExit>
      <Alert
        severity="info"
        variant="standard"
        action={
          <IconButton
            aria-label="close"
            color="inherit"
            size="small"
            onClick={() => {
              deleteNewNotification(notification.notificationId);
            }}
          >
            <CloseOutlined fontSize="inherit" />
          </IconButton>
        }
      >
        <AlertTitle>알림</AlertTitle>
        <Typography fontSize={14}>{notificationFiftyLength}</Typography>

        {notification.notificationType === "BOOKMARK" ? <Button onClick={() => handleToUrl(notification.toUrl)}>담긴 시 보러가기</Button> : null}
      </Alert>
    </Collapse>
  );
}
