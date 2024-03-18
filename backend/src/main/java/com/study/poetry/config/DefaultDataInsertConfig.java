package com.study.poetry.config;

import com.study.poetry.entity.Category;
import com.study.poetry.entity.Member;
import com.study.poetry.entity.ReportReason;
import com.study.poetry.entity.Role;
import com.study.poetry.repository.CategoryRepository;
import com.study.poetry.repository.MemberRepository;
import com.study.poetry.repository.ReportReasonRepository;
import com.study.poetry.repository.RoleRepository;
import com.study.poetry.utils.enums.Gender;
import com.study.poetry.utils.enums.ReportReasonType;
import com.study.poetry.utils.enums.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


//  애플리케이션 실행 시, 기본적인 데이터를 DB에 저장하기 위한 클래스
@Configuration
public class DefaultDataInsertConfig {
  private final PasswordEncoder passwordEncoder;

  public DefaultDataInsertConfig(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }


  @Bean
  //  기본 사용자 역할 2개 생성
  public CommandLineRunner insertRoles(RoleRepository roleRepository) {
    return args -> {
      if (roleRepository.count() == 0) {

        Role adminRole = new Role(1L, UserRole.ROLE_ADMIN);
        Role userRole = new Role(2L, UserRole.ROLE_USER);

        roleRepository.save(userRole);
        roleRepository.save(adminRole);
      }
    };
  }

  @Bean
  // 기본 카테고리 7개 생성
  public CommandLineRunner insertCategory(CategoryRepository categoryRepository) {
    return args -> {
      List<String> categories = List.of("삶", "사랑", "이별", "생활", "가족", "웃음", "기타");
      if (categoryRepository.count() == 0) {
        categories.stream()
            .map(category ->
                new Category(categories.indexOf(category)+1, category, (long) categories.indexOf(category)+1))
            .forEach(categoryRepository::save);
      }
    };
  }

  @Bean
  //신고 사유 5개 생성
  public CommandLineRunner insertReportReason(ReportReasonRepository reportReasonRepository) {
    return args -> {
      if (reportReasonRepository.count() == 0) {
        ReportReason reportReason1 = new ReportReason(1L, ReportReasonType.MEMBER_NAME);
        ReportReason reportReason2 = new ReportReason(2L, ReportReasonType.MEMBER_PROFILE_IMAGE);
        ReportReason reportReason3 = new ReportReason(3L, ReportReasonType.POEM_CONTENT);
        ReportReason reportReason4 = new ReportReason(4L, ReportReasonType.POEM_BACKGROUND_IMAGE);
        ReportReason reportReason5 = new ReportReason(5L, ReportReasonType.POEM_IRRELEVANT_CATEGORY);

        List<ReportReason> reasons = List.of(reportReason1, reportReason2, reportReason3, reportReason4, reportReason5);
        reportReasonRepository.saveAll(reasons);
      }
    };
  }

  @Bean
  //  기본관리자 계정 1개 생성
  public CommandLineRunner insertAdminUser(MemberRepository memberRepository) {
    return args -> {
      if (!memberRepository.existsByEmail("admin@poetry.com")) {

        Role adminRole = new Role(1L, UserRole.ROLE_ADMIN);
        Member admin = Member.builder()
            .email("admin@poetry.com")
            .name("관리자")
            .password(passwordEncoder.encode("123123123"))
            .regdate(LocalDateTime.now())
            .gender(Gender.MALE)
            .phone("01012341234")
            .roles(Set.of(adminRole))
            .build();

        memberRepository.save(admin);
      }
    };
  }
//----------------------------------------------------------------------------------------
//
//  @Bean
//  public CommandLineRunner insertDummyUser(MemberRepository memberRepository) {
//    return args -> {
//      if (memberRepository.count() < 2) {
//        Role adminRole = new Role(1L, UserRole.ROLE_ADMIN);
//        Role userRole = new Role(2L, UserRole.ROLE_USER);
//        for (int i = 1; i <= 100; i++) {
//          Role role = i <= 11 ? adminRole : userRole;
//          Member user = Member.builder()
//              .memberId((long) i)
//              .email("user" + i + "@test.com")
//              .name("user" + i)
//              .password(passwordEncoder.encode("123123123"))
//              .regdate(LocalDateTime.now())
//              .gender(Gender.MALE)
//              .phone("010111111" + i)
//              .roles(Set.of(role))
//              .build();
//          memberRepository.save(user);
//        }
//        List<Member> memberWithUpdatedRegDate = memberRepository.findAll()
//            .stream()
//            .peek(member -> member.setRegdate(generateRandomDateTime()))
//            .toList();
//        memberRepository.saveAll(memberWithUpdatedRegDate);
//
//      }
//    };
//  }
//
//
//  @Bean
//  public CommandLineRunner insertDummyPoem(PoemSettingsRepository poemSettingsRepository, PoemRepository poemRepository, MemberRepository memberRepository) {
//
//    return args -> {
//      if (poemRepository.count() == 0) {
//        for (int i = 1; i <= 300; i++) {
//          long userNumber = (long) (Math.random()*49+30);
//          Member byEmail;
//          if(memberRepository.existsById(userNumber)){
//             byEmail = memberRepository.findByEmail("user" + userNumber + "@test.com").get();
//          } else continue;
//          Random random = new Random();
//          PoemSettings poemSettings = PoemSettings.builder().fontFamily("궁서체").contentFontSize("small").titleFontSize("h4").backgroundOpacity(1.0f).build();
//          poemSettingsRepository.save(poemSettings);
//          Poem poem = Poem.builder()
//              .content("content" + i)
//              .categoryId(random.nextInt(6) + 1)
//              .title("title"+i)
//              .member(byEmail)
//              .description("description" + i)
//              .poemSettings(poemSettings)
//              .build();
//          poemRepository.save(poem);
//
//        }
//        List<Poem> poemWithUpdatedWriteDate = poemRepository.findAll()
//            .stream()
//            .peek(poem -> poem.setWriteDate(generateRandomDateTime()))
//            .toList();
//        poemRepository.saveAll(poemWithUpdatedWriteDate);
//      }
//    };
//  }
//
//
//  @Bean
//  public CommandLineRunner insertDummyReport(PoemRepository poemRepository,MemberRepository memberRepository, ReportRepository reportRepository, ReportReasonRepository reportReasonRepository, ReportDetailsRepository reportDetailsRepository) {
//
//    return args -> {
//      if (reportRepository.count() == 0) {
//        List<ReportReason> reportReasonList = reportReasonRepository.findAll();
//
//
//        for (int i = 1; i < 150; i++) {
//          Poem poem = poemRepository.findById((long) i).get();
//
//
//          ReportStatus reportStatus = i % 3 == 0  ? ReportStatus.REPORTED : i % 2 == 1 ? ReportStatus.UNDER_REVIEW : ReportStatus.DONE;
//
//          Report report = Report.builder()
//              .poem(poem)
//              .reportStatus(reportStatus)
//              .build();
//          Report savedReport = reportRepository.save(report);
//          List<ReportDetails> details = new ArrayList<>();
//
//          int reportedCount = (int) (Math.random() * 10);
//
//          for(int j = 0; j<reportedCount; j++){
//            long userNumber = (long) (Math.random()*49+30);
//            Member member = memberRepository.findByEmail("user" + userNumber + "@test.com").get();
//            int reasonNumber1 = (int) (Math.random()*3+1);
//            int reasonNumber2 = reasonNumber1+1;
//            Set<ReportReason> selectedReason = Set.of(reportReasonList.get(reasonNumber1), reportReasonList.get(reasonNumber2));
//            ReportDetails reportDetails = ReportDetails.builder()
//                .reportComment("comment"+j)
//                .member(member)
//                .reportReasons(selectedReason)
//                .build();
//            reportDetails.setReport(savedReport);
//            ReportDetails savedReportDetails = reportDetailsRepository.save(reportDetails);
//            details.add(savedReportDetails);
//          }
//
//          savedReport.setReportDetails(details);
//          reportRepository.save(savedReport);
//        }
//        List<ReportDetails> reportDetailsWithUpdatedReportDate = reportDetailsRepository.findAll()
//            .stream()
//            .peek(report -> report.setReportDate(generateRandomDateTime()))
//            .toList();
//        reportDetailsRepository.saveAll(reportDetailsWithUpdatedReportDate);
//
//        List<Report> reportWithUpdatedReportDate = reportRepository.findAll()
//            .stream()
//            .peek(report -> report.setCreationDate(generateRandomDateTime()))
//            .toList();
//
//        List<Report> collect = reportWithUpdatedReportDate.stream().map(report -> {
//          int i = (int) (Math.random() * 5) + 1;
//          Optional<Member> byEmail = memberRepository.findByEmail("user" + i + "@test.com");
//          report.setDoneBy(byEmail.get());
//          return report;
//        }).toList();
//
//        reportRepository.saveAll(collect);
//      }
//    };
//  }
//
//
//  private LocalDateTime generateRandomDateTime() {
//
//    LocalDateTime now = LocalDateTime.now();
//
//    LocalDateTime startDateTime = now.minusYears(1);
//    LocalDateTime endDateTime = startDateTime.plusYears(1);
//
//
//    long startEpochSecond = startDateTime.toEpochSecond(java.time.ZoneOffset.UTC);
//    long endEpochSecond = endDateTime.toEpochSecond(java.time.ZoneOffset.UTC);
//
//    long randomEpochSecond = startEpochSecond + (long) (Math.random() * (endEpochSecond - startEpochSecond));
//
//    return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, java.time.ZoneOffset.UTC);
//  }
}
