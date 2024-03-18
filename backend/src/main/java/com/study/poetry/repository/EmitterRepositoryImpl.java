package com.study.poetry.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
@Repository
public class EmitterRepositoryImpl implements EmitterRepository{
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

  @Override
  public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
    emitters.put(emitterId,sseEmitter);
    return sseEmitter;
  }

  @Override
  public void saveEventCache(String emitterId, Object event) {
    eventCache.put(emitterId,event);
  }

  @Override
  public Map<String, SseEmitter> findAllEmitterStartWithByEmail(String email) {
    return emitters.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(email))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Map<String, Object> findAllEventCacheStartWithByEmail(String memberId) {
    return eventCache.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(memberId))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public void deleteById(String emitterId) {
    emitters.remove(emitterId);
  }

  @Override
  public void deleteAllEmitterStartWithEmail(String email) {
    emitters.forEach(
        (key,emitter) -> {
          if(key.startsWith(email)){
            emitters.remove(key);
          }
        }
    );
  }

  @Override
  public void deleteAllEventCacheStartWithEmail(String email) {
    eventCache.forEach(
        (key,emitter) -> {
          if(key.startsWith(email)){
            eventCache.remove(key);
          }
        }
    );
  }
}