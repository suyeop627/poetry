package com.study.poetry.controller;

import com.study.poetry.entity.Member;
import com.study.poetry.service.EmitterService;
import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.notification.NotificationDto;
import com.study.poetry.service.MemberService;
import com.study.poetry.service.NotificationService;
import com.study.poetry.utils.annotation.TokenToMemberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequestMapping("/notifications")
@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
  private final NotificationService notificationService;
  private final MemberService memberService;
  private final EmitterService emitterService;

  //회원 로그인 시, sse 수신을 위한 서버와 연결 요청 처리
  //lastEventId : 첫 연결시에는 포함되지 않으나, 재연결시 lastEventId가 포함되어 subscribe요청이 전달됨
  //lastEventId가 존재할 경우, 캐시된 메시지 중 lastEventId의 시간값보다 더 이후시간대의 id로 캐싱된 메시지가 존재하는지 비교하여,
  //재연결과정에서 유실된 메시지가 존재하는지 확인할 수 있음
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> subscribe(@TokenToMemberInfo LoginMemberInfo loginMemberInfo,
                                              @RequestParam(value = "lastEventId", required = false, defaultValue = "") String lastEventId) {

    log.info("New subscription request received with Last-Event-ID({}) from {}", lastEventId, loginMemberInfo);
    return ResponseEntity.ok(emitterService.subscribe(loginMemberInfo, lastEventId));
  }

  //기존 알림 내역 조회 요청 처리
  @GetMapping
  public ResponseEntity<?> getNotification(@TokenToMemberInfo LoginMemberInfo loginMemberInfo) {
    log.info("member(id: {}) requests get notifications", loginMemberInfo.getMemberId());
    return ResponseEntity.ok(notificationService.findNotificationByReceiver(loginMemberInfo));
  }

  //알림 목록 내역 수정 요청 처리
  //사용자가 삭제를 선택한경우, 해당 알림을 삭제하며, 읽은 알림은 읽음처리 함
  @PutMapping
  public ResponseEntity<?> updateNotifications(@TokenToMemberInfo LoginMemberInfo loginMemberInfo,
                                               @RequestBody List<NotificationDto> notificationDtoList) {
    log.info("Member (id: {}) requests update notification.", loginMemberInfo.getMemberId());
    Member receiver = memberService.getMemberByIdOrThrow(loginMemberInfo.getMemberId());
    notificationService.updateNotifications(receiver, notificationDtoList);
    return ResponseEntity.ok().build();
  }
}
