package com.study.poetry.service;

import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.notification.NotificationDto;
import com.study.poetry.repository.EmitterRepositoryImpl;
import com.study.poetry.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterService {
  //SSE 연결 시간
  private final long TIMEOUT = 5 * 60 * 1000L;
  //emit - 내뿜다 발행하다 내보내다
  //emitter - 발행인
  private final EmitterRepositoryImpl emitterRepository;

  //사용자의 sse 연결 요청 처리
  public SseEmitter subscribe(LoginMemberInfo loginMemberInfo, String lastEventId) {

    String emitterId = String.format("%s_%s", loginMemberInfo.getEmail(), System.currentTimeMillis());
    log.info("Request received for subscribe. Created emitterId: {}", emitterId);
    SseEmitter emitter;

    //새 연결이 시작됐는데, 기존 연결이 남아있지 않도록 삭제후 처리
    if (!emitterRepository.findAllEmitterStartWithByEmail(loginMemberInfo.getEmail()).isEmpty()) {
      emitterRepository.deleteAllEmitterStartWithEmail(loginMemberInfo.getEmail());
    }

    emitter = emitterRepository.save(emitterId, new SseEmitter(TIMEOUT));

    defineSseEmitterCallbackMethod(emitterId, emitter);

    sendConnectionSuccessMessage(emitterId, emitter);

    sendCachedMessageIfHasLostData(loginMemberInfo, lastEventId, emitter);
    return emitter;
  }

  //SseEmitter의 콜백합수 정의
  private void defineSseEmitterCallbackMethod(String emitterId, SseEmitter emitter) {
    emitter.onCompletion(() -> {//연결이 종료됐을때
      log.info("SSE onCompletion callback " + emitterId + " is deleted");
      emitterRepository.deleteById(emitterId);
    });
    emitter.onTimeout(() -> {//timeout으로 지정한  시간이 초과됐을때
      log.info("SSE onTimeout callback " + emitterId + " is deleted");
      emitter.complete();
    });
    emitter.onError((error) -> {//연결 중 에러발생
      log.error("SSE onError callback " + emitterId + " is deleted", error);
      emitterRepository.deleteById(emitterId);
      emitter.complete();
    });
  }

  //연결 직후 더미데이터 전송
  // (503(SERVICE_UNAVAILABLE) 에러 발생 방지 - 연결 이후 아무런 메시지가 전달되지 않을 경우 발생)
  private void sendConnectionSuccessMessage(String emitterId, SseEmitter emitter) {
    NotificationDto notificationDto =
        NotificationDto.builder()
            .notificationType(NotificationType.CONNECTION)
            .build();

    sendToClient(emitter, emitterId, notificationDto);
  }

  //캐싱된 메시지가 있을 경우 모두 전송(lastEventId가 있을 경우, 캐싱된 메시지 중 lastEventId보다 더 이후 시간인 메시지를 모두 전송)
  private void sendCachedMessageIfHasLostData(LoginMemberInfo loginMemberInfo, String lastEventId, SseEmitter emitter) {
    if (hasLostData(lastEventId)) {
      Map<String, Object> cashedEvents =
          emitterRepository.findAllEventCacheStartWithByEmail(loginMemberInfo.getEmail());
      //entrySet -> 키와 값을 쌍으로 가져옴(vs keySet():키만 가져옴)
      cashedEvents.entrySet().stream()
          //캐시된 emitterId보다 lastEventId의 시간값이 더 작을 경우(받은 데이터보다 캐시된 데이터의 시간이 더 이후일 경우)
          .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
          .forEach(entry ->
              sendToClient(emitter, entry.getKey(), entry.getValue()));

      //미수신 메시지 전송 후 캐시 삭제
      emitterRepository.deleteAllEventCacheStartWithEmail(loginMemberInfo.getEmail());
    }
  }

  //lastEventId 존재 유무 확인
  private boolean hasLostData(String lastEventId) {
    return !lastEventId.isEmpty();
  }


  //Notification 의 수신자에 해당하는 emitter 조회및 메시지 발송
  public void sendNotification(NotificationDto notificationDto) {
    //로그인한 유저의 emitter를 모두 조회
    Map<String, SseEmitter> sseEmitters =
        emitterRepository.findAllEmitterStartWithByEmail(notificationDto.getReceiver().getEmail());

    //로그인한 유저의 emitter에 대해 알림을 캐싱하고 전송
    sseEmitters.forEach((key, emitter) -> {
      emitterRepository.saveEventCache(key, notificationDto);
      sendToClient(emitter, key, notificationDto);
    });
  }

  //클라이언트에 메시지 발송
  private void sendToClient(SseEmitter emitter, String id, Object data) {
    try {
      emitter.send(
          SseEmitter
              .event()
              .id(id)
              .name("sse")
              .data(data));

    } catch (ClientAbortException e) {//ioexception의 하위 exception이라서 위로 올림
      log.error(e.getMessage());
    } catch (IOException e) {
      emitterRepository.deleteById(id);
      emitter.completeWithError(e);
    }
  }
}
