package com.study.poetry.entity;

import com.study.poetry.utils.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
//알림 정보 저장 엔티티
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;

  @OnDelete(action = OnDeleteAction.CASCADE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member receiver;

  @Column(length = 500)
  private String content;

  @Column(length=100)
  private String toUrl;

  @Column
  @Enumerated(EnumType.STRING)
  private NotificationType notificationType;

  @Column
  private boolean isRead=false;

  @CreationTimestamp
  private LocalDateTime occurredAt;
}
