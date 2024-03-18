package com.study.poetry.repository;

import com.study.poetry.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findAllByReceiver_MemberIdOrderByOccurredAtDesc(Long memberId);
}
