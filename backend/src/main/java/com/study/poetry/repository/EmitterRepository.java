package com.study.poetry.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
@Repository
public interface EmitterRepository {
  SseEmitter save(String emitterId, SseEmitter sseEmitter);
  void saveEventCache(String emitterId, Object event);
  Map<String, SseEmitter> findAllEmitterStartWithByEmail(String memberId);
  Map<String,Object> findAllEventCacheStartWithByEmail(String memberId);

  void deleteById(String emitterId);

  void deleteAllEmitterStartWithEmail(String memberId);

  void deleteAllEventCacheStartWithEmail(String memberId);
}
