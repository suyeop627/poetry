import { useDispatch, useSelector } from "react-redux";
import { login, logout, selectAuth } from "../../../auth/authSlice";
import { useState } from "react";
import { useEffect } from "react";
import { EventSourcePolyfill } from "event-source-polyfill";
import axios from "axios";

//서버와 SSE 연결 담당
export default function EventsourceManager({ setNotificationList, setNewNotificationList }) {
  let eventSource = null;
  const { user } = useSelector(selectAuth);
  const [isTokenExpired, setIsTokenExpired] = useState(false);

  const dispatch = useDispatch();
  //컴포넌트 언마운트 시, 연결 종료
  useEffect(() => {
    return () => {
      if (eventSource) {
        closeConnection();
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  //사용자의 토큰이 존재하며, 토큰이 만료되지 않은경우 (재)연결
  useEffect(() => {
    if (user.accessToken != null) {
      if (!isTokenExpired) {
        connect();
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isTokenExpired]);

  //토큰이 만료된경우 토큰 재발행 요청
  useEffect(() => {
    if (isTokenExpired) {
      reIssueAccessToken();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isTokenExpired]);

  //EventSource 연결 종료
  const closeConnection = () => {
    eventSource.close();
    eventSource = null;
  };

  //Sse연결 요청
  const connect = () => {
    if (eventSource !== null) {
      closeConnection();
    }
    const newEventSource = new EventSourcePolyfill("http://localhost:8080/notifications/subscribe", {
      headers: { Authorization: "Bearer " + user.accessToken },
      heartbeatTimeout: 5 * 60 * 1000,
    });

    newEventSource.onopen = (event) => {
      //console.log("connection opened");
    };

    //연결 중 에러 발생 시 처리
    newEventSource.onerror = (error) => {
      if (error.status === 401) {
        setIsTokenExpired(true);
        closeConnection();
      } else if (error.error?.message === "Failed to fetch") {
        alert("서버측 문제로 인해 알림을 전송받을 수 없습니다.");
        closeConnection();
      } else {
        // connect(); 새연결 실행 시 lastEventId 전달 안됨
        //아무처리도 하지 않으면 EventSource가 자동으로 재연결 시도
      }
    };
    //서버에서 보낸 메시지 존재 시, 알림 및 실시간 알림내역에 추가
    newEventSource.addEventListener("sse", (event) => {
      const newNotification = JSON.parse(event.data);
      if (newNotification.notificationType !== "CONNECTION") {
        setNotificationList((prevNotifications) => [newNotification, ...prevNotifications]);
        setNewNotificationList((prevNotifications) => [newNotification, ...prevNotifications]);
      }
    });

    eventSource = newEventSource;
  };
  //access token 재발행 요청
  const reIssueAccessToken = async () => {
    try {
      const refreshTokenId = user.refreshTokenId;
      const memberId = user.memberId;
      const reissueAccessTokenRequestObj = {
        refreshTokenId: refreshTokenId,
        memberId: memberId,
      };
      const response = await axios.put("http://localhost:8080/auth", reissueAccessTokenRequestObj);
      if (response.status === 200) {
        const { memberId, name, accessToken, refreshTokenId, roles } = response.data;
        dispatch(login({ memberId, name, accessToken, refreshTokenId, roles }));
        setIsTokenExpired(false);
      }
    } catch (error) {
      const errorResponse = error.response;
      if (errorResponse.status === 401 && errorResponse.headers.jwtexception && errorResponse.headers.jwtexception === "EXPIRED_REFRESH_TOKEN") {
        alert("인증 토큰이 만료됐습니다. 로그인페이지로 이동합니다.");
        dispatch(logout());
        window.location.href = "/login";
      } else {
        alert("요청 처리 중 오류가 발생했습니다.");
      }
    }
  };
}
