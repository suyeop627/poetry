package com.study.poetry.dto.notification;

import com.study.poetry.dto.member.MemberDto;
import com.study.poetry.utils.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
//알림 전달 시, 응답 dto
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
  private Long notificationId;
  private MemberDto receiver;

  private String content;

  private String toUrl;

  private NotificationType notificationType;

  private boolean isRead=false;

  private boolean isDeleted=false;

  private LocalDateTime occurredAt;
}
