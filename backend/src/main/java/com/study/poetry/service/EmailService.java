package com.study.poetry.service;

import com.study.poetry.dto.member.EmailVerificationRequestDto;
import com.study.poetry.entity.EmailVerificationCode;
import com.study.poetry.exception.UnableToSendEmailException;
import com.study.poetry.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender emailSender;
  private final EmailVerificationCodeRepository emailVerificationCodeRepository;


  //인증코드와 유효기간 정보를 포함한 이메일 발송
  public void sendEmailVerificationMail(String toEmail) {
    SimpleMailMessage emailForm = createEmailForm(toEmail);

    try {


      emailSender.send(emailForm);

    } catch (RuntimeException e) {
      log.error("An exception occurred in EmailService while sending Email: {}, " +
          "title: {}, text: {}", toEmail, emailForm.getSubject(), emailForm.getText());
      e.printStackTrace();
      throw new UnableToSendEmailException(e.getMessage());
    }
  }

  //랜덤한 인증코드 생성
  private String generateVerificationKey() {
    Random random = new Random();
    StringBuilder key = new StringBuilder();

    //문자 2자리
    for (int i = 0; i < 2; i++) {
      int index = random.nextInt(26) + 65;
      key.append((char) index);
    }
    //숫자 2자리
    for (int i = 0; i < 4; i++) {
      int numIndex = random.nextInt(10);
      key.append(numIndex);
    }
    return key.toString();
  }

  // 발송할 이메일 양식 생성
  private SimpleMailMessage createEmailForm(String toEmail) {
    String key = generateVerificationKey();
    LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(10);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
    String formattedDateTime = expiredAt.format(formatter);

    String title = "[Poe-Try] 인증번호 발송";
    String text = "인증 번호는 " + key + " 입니다.\n";
    text += "유효기간 내 인증번호를 입력해주세요.\n";
    text += "인증번호 만료 일시 : " + formattedDateTime;


    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject(title);
    message.setText(text);

    saveEmailVerificationCode(toEmail, key, expiredAt);

    return message;
  }

  //인증코드와 이메일, 유효기간을 db에 저장
  private void saveEmailVerificationCode(String toEmail, String key, LocalDateTime expiredAt) {
    deleteEmailAlreadySent(toEmail);
    EmailVerificationCode emailVerificationCode =
        EmailVerificationCode.builder()
            .code(key)
            .email(toEmail)
            .expiredAt(expiredAt)
            .build();

    emailVerificationCodeRepository.save(emailVerificationCode);
    log.info("The email verification code for {} is saved.", toEmail);
  }


  //인증하려는 이메일에 해당하는 기존 인증번호 삭제
  private void deleteEmailAlreadySent(String toEmail) {
    emailVerificationCodeRepository.deleteByEmail(toEmail);
  }

  //입력받은 인증번호와 기존 발송한 인증번호와 일치여부 확인
  public boolean isValidVerifyCode(EmailVerificationRequestDto emailVerificationRequestDto) {
    Optional<EmailVerificationCode> optionalCode =
        emailVerificationCodeRepository.findByEmailAndCode(emailVerificationRequestDto.getEmail(), emailVerificationRequestDto.getVerifyCode());

    if(optionalCode.isPresent()){
      EmailVerificationCode emailVerificationCode = optionalCode.get();

      if(emailVerificationCode.getExpiredAt().isAfter(LocalDateTime.now())){
        emailVerificationCodeRepository.deleteById(emailVerificationCode.getId());
        return true;
      }
    }
    return false;
  }
}
