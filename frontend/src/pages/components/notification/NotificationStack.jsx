import { Stack } from "@mui/material";
import NotificationAlert from "./NotificationAlert";

//실시간 알림내역 관리
export default function NotificationStack({ notifications, setNewNotificationList }) {
  return (
    <Stack sx={{ position: "absolute", top: 80, right: 20, width: 500, zIndex: 1000 }} spacing={2}>
      {notifications.map((notification, index) => (
        <NotificationAlert
          key={notification.notificationId}
          notification={notification}
          setNewNotificationList={setNewNotificationList}
          newNotificationList={notifications}
        ></NotificationAlert>
      ))}
    </Stack>
  );
}
