import axios from "axios";
import store from "../store/store";
import { logout, login } from "../auth/authSlice";

//요청에 Jwt토큰을 함께 보내며, 응답에서 토큰과 관련된 예외처리 담당
const jwtAxios = axios.create();
jwtAxios.interceptors.request.use(
  (config) => {
    const accessToken = store.getState().auth.user.accessToken;
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

jwtAxios.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const errorResponse = error.response;
    if (errorResponse.status === 401) {
      if (errorResponse.headers.jwtexception) {
        return handleJwtException(errorResponse.headers.jwtexception, error);
      } else {
        alert("권한이 없습니다.");
        window.location.href = "/";
      }
    }
    return Promise.reject(error);
  }
);
const handleJwtException = async (jwtException, error) => {
  if (jwtException === "EXPIRED_ACCESS_TOKEN") {
    // console.log("EXPIRED_ACCESS_TOKEN");
    const response = await requestReissueAccessToken(error);
    return response;
  } else if (jwtException === "TOKEN_NOT_FOUND") {
    // console.log("TOKEN_NOT_FOUND");
    alert("인증 토큰을 찾지 못했습니다. 로그인페이지로 이동합니다.");
  } else if (jwtException === "INVALID_TOKEN") {
    // console.log("INVALID_TOKEN");
    alert("인증 토큰 형식이 올바르지 않습니다.. 로그인페이지로 이동합니다.");
  } else if (jwtException === "EXPIRED_REFRESH_TOKEN") {
    // console.log("EXPIRED_REFRESH_TOKEN");
    alert("인증 토큰이 만료됐습니다. 로그인페이지로 이동합니다.");
  } else if (jwtException === "UNKNOWN_ERROR") {
    // console.log("UNKNOWN_ERROR");
    alert("인증 토큰을 처리하는 과정에서 서버에 문제가 발생했습니다. 로그인페이지로 이동합니다.");
  } else if (jwtException === "INVALID_SIGNATURE") {
    alert("인증 토큰 서명에 이상이 발견됐습니다. 로그인페이지로 이동합니다.");
    // console.log("INVALID_SIGNATURE");
  }
  store.dispatch(logout());
  window.location.href = "/login";
};
const requestReissueAccessToken = async (error) => {
  const refreshTokenId = store.getState().auth.user.refreshTokenId;
  const memberId = store.getState().auth.user.memberId;
  const reissueAccessTokenRequestObj = {
    refreshTokenId: refreshTokenId,
    memberId: memberId,
  };
  try {
    const response = await axios.put("http://localhost:8080/auth", reissueAccessTokenRequestObj);
    if (response.status === 200) {
      const { memberId, name, accessToken, refreshTokenId, roles } = response.data;
      store.dispatch(login({ memberId, name, accessToken, refreshTokenId, roles }));
      const originalRequest = error.config;
      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return jwtAxios(originalRequest).then((response) => {
        return response;
      });
    }
  } catch (error) {
    const errorResponse = error.response;
    if (errorResponse.status === 401) {
      if (errorResponse.headers.jwtexception) {
        handleJwtException(errorResponse.headers.jwtexception);
      }
    }
  }
};
export default jwtAxios;
