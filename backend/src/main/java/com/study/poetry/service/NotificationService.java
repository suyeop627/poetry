package com.study.poetry.service;

import com.study.poetry.entity.*;
import com.study.poetry.dto.auth.LoginMemberInfo;
import com.study.poetry.dto.notification.NotificationDto;
import com.study.poetry.repository.NotificationRepository;
import com.study.poetry.utils.enums.NotificationType;
import com.study.poetry.utils.enums.ReportStatus;
import com.study.poetry.utils.enums.RestrictionType;
import com.study.poetry.utils.mapper.MemberDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
  private final MemberDtoMapper memberDtoMapper;
  private final NotificationRepository notificationRepository;
  private final EmitterService emitterService;

  //북마크 알림 저장 및 북마크 대상 게시글 작성자에게 알림 전송
  public void saveBookmarkNotification(Bookmark bookmark) {
    Notification notification = Notification.builder()
        .receiver(bookmark.getPoem().getMember())
        .notificationType(NotificationType.BOOKMARK)
        .toUrl("/poems/" + bookmark.getPoem().getPoemId())
        .content(generateContentOfBookmark(bookmark))
        .build();

    Notification savedNotification = notificationRepository.save(notification);
    NotificationDto notificationDto = notificationEntityToDto(savedNotification);
    emitterService.sendNotification(notificationDto);
  }

  //북마크 알림 내용 생성
  private String generateContentOfBookmark(Bookmark bookmark) {
    return String.format("'%s' 님께서  '%s' 님의 시(%s)를 담아갔습니다.",
        bookmark.getMember().getName(),
        bookmark.getPoem().getMember().getName(),
        bookmark.getPoem().getTitle());
  }

  //신고내역 상태 '검토중' 변경 알림 저장 및 신고자 모두에게 알림 전송
  public void saveReportUnderReviewNotificationAndSend(Report report) {

    List<ReportDetails> reportDetailsList = report.getReportDetails();
    List<Notification> notificationList =
        reportDetailsList.stream()
            .map(reportDetails ->
                Notification.builder()
                    .receiver(reportDetails.getMember())
                    .notificationType(NotificationType.REPORT)
                    .toUrl("/poems/" + report.getPoem().getPoemId())
                    .content(
                        generateContentOfReportStatusChange(report.getPoem(), reportDetails, ReportStatus.UNDER_REVIEW))
                    .build()
            ).toList();
    sendNotificationList(notificationList);
  }

  //신고 내역 상태 '처리완로' 변경 알림을 저장 및 신고자 모두에게 알림 전송
  public void saveReportResultNotificationAndSend(Report report, List<RestrictionType> restrictionTypeList) {

    List<ReportDetails> reportDetailsList = report.getReportDetails();

    List<Notification> notificationList = reportDetailsList.stream().map(reportDetails ->
        Notification.builder()
            .receiver(reportDetails.getMember())
            .notificationType(NotificationType.REPORT)
            .toUrl("/poems/" + report.getPoem().getPoemId())
            .content(
                generateContentOfReportResult(report.getPoem(), reportDetails, report.getReportStatus(), restrictionTypeList))
            .build()
    ).toList();
    sendNotificationList(notificationList);
  }

  //notification 목록을 모두 저장하며, 각 notification을 수신자에게 알림 전송
  private void sendNotificationList(List<Notification> notificationList) {
    notificationList.forEach(notification -> {
          Notification savedNotification = notificationRepository.save(notification);
          NotificationDto notificationDto = notificationEntityToDto(savedNotification);
          emitterService.sendNotification(notificationDto);
        }
    );
  }


  //신고상태 변경 알림 내용 생성
  private String generateContentOfReportStatusChange(Poem poem, ReportDetails reportDetails, ReportStatus reportStatus) {
    return String.format("'%s' 님께서 신고하신 '%s' 님의 시(%s)에 대한 신고처리 상태가 %s로 변경됐습니다.",
        reportDetails.getMember().getName(), poem.getMember().getName(),
        poem.getTitle(), reportStatus.getDescription());
  }

  //신고처리 내역을 담은 알림 내용 생성
  private String generateContentOfReportResult(Poem poem,
                                               ReportDetails reportDetails,
                                               ReportStatus reportStatus,
                                               List<RestrictionType> restrictionTypeList) {

    String reportStatusChangeContent = generateContentOfReportStatusChange(poem, reportDetails, reportStatus);
    StringBuffer stringBuffer = new StringBuffer(reportStatusChangeContent);

    String content = "\n\n※제재내역 : \n";

    stringBuffer.append(content);

    restrictionTypeList.forEach(restrictionType -> stringBuffer.append(restrictionType.getMessage()).append(", "));
    stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());

    return stringBuffer.toString();
  }


  //수신자의 모든 알림내역 조회
  public List<NotificationDto> findNotificationByReceiver(LoginMemberInfo loginMemberInfo) {
    List<Notification> allByReceiver_memberId = notificationRepository.findAllByReceiver_MemberIdOrderByOccurredAtDesc(loginMemberInfo.getMemberId());
    return allByReceiver_memberId.stream()
        .map(this::notificationEntityToDto)
        .toList();
  }

  //알림내역 수정 (읽음처리 또는 삭제처리)
  @Transactional
  public void updateNotifications(Member receiver, List<NotificationDto> notificationDtoList) {
    for (NotificationDto notificationDto : notificationDtoList) {
      if (notificationDto.isDeleted()) {
        notificationRepository.deleteById(notificationDto.getNotificationId());
      } else {
        notificationRepository.save(notificationDtoToEntity(notificationDto, receiver));
      }
    }
  }

  //NotificationDto => Notification 변환
  private Notification notificationDtoToEntity(NotificationDto notificationDto, Member receiver) {
    return Notification.builder()
        .notificationId(notificationDto.getNotificationId())
        .content(notificationDto.getContent())
        .toUrl(notificationDto.getToUrl())
        .notificationType(notificationDto.getNotificationType())
        .isRead(notificationDto.isRead())
        .receiver(receiver)
        .occurredAt(notificationDto.getOccurredAt())
        .build();
  }

  //Notification => NotificationDto 변환
  private NotificationDto notificationEntityToDto(Notification notification) {
    return NotificationDto.builder()
        .notificationId(notification.getNotificationId())
        .receiver(memberDtoMapper.apply(notification.getReceiver()))
        .notificationType(notification.getNotificationType())
        .content(notification.getContent())
        .toUrl(notification.getToUrl())
        .occurredAt(notification.getOccurredAt())
        .isRead(notification.isRead())
        .build();
  }
}
