package com.study.poetry.repository;

import com.study.poetry.entity.Member;
import com.study.poetry.dto.admin.MemberStatisticsInterface;
import com.study.poetry.utils.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String username);
  boolean existsByEmail(String userEmail);
  boolean existsByPhone(String phone);
  //name
  Page<Member> findByNameContainingIgnoreCaseOrderByRegdateDesc(String name, PageRequest pageRequest);
  //email
  Page<Member> findByEmailContainingIgnoreCaseOrderByRegdateDesc(String email, PageRequest pageRequest);
  //phone
  Page<Member> findByPhoneContainingIgnoreCaseOrderByRegdateDesc(String name, PageRequest pageRequest);
  //role
  Page<Member> findByRoles_NameOrderByRegdateDesc(UserRole role, PageRequest pageRequest);

  @Query(value = """
      SELECT
          SUM(
              CASE
                  WHEN m.regdate>=DATE_SUB(CURDATE(),INTERVAL 1 MONTH)
                      THEN 1
                  ELSE 0
              END) AS newMembersCountOfRecentMonth,
          ROUND(SUM(
              CASE
                  WHEN m.regdate>=DATE_SUB(CURDATE(), INTERVAL  13 MONTH )
                      AND m.regdate<DATE_SUB(LAST_DAY(CURDATE()), INTERVAL  1 MONTH )
                      THEN 1
                  ELSE 0
              END)/12,2) AS monthlyNewMembersCount,
          COUNT(DISTINCT m.member_id) AS totalMembersCount
      FROM member m
          JOIN member_role mr ON m.member_id = mr.member_id
          JOIN role r ON r.role_id = mr.role_id
      WHERE r.name!='ROLE_ADMIN';""", nativeQuery = true)
  MemberStatisticsInterface selectMemberStatistics();

  List<Member> findByRoles_NameOrderByMemberId(UserRole role);



  //

//  @Query(value = """
//      SELECT ROUND(AVG(member_count), 1) AS monthly_new_member
//      FROM (SELECT COUNT(m.member_id) AS member_count
//            FROM member m
//                     JOIN member_role mr ON m.member_id = mr.member_id
//                     JOIN role r on mr.role_id = r.role_id
//            WHERE regdate >= DATE_SUB(NOW(), INTERVAL 13 MONTH)
//              AND regdate < DATE_SUB(LAST_DAY(NOW()), INTERVAL 1 MONTH)
//              AND r.name != 'ROLE_ADMIN'
//            GROUP BY DATE_FORMAT(regdate, '%Y%m')) AS grouped_member""",
//      nativeQuery = true)
//  float countMonthlyNewMembersAverageWithoutAdmin();
//
//  @Query(value = """
//      SELECT COUNT(m.member_id) AS monthly_new_member
//      FROM member m
//               JOIN member_role mr ON m.member_id = mr.member_id
//               JOIN role r ON mr.role_id = r.role_id
//      WHERE regdate >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
//        AND r.name != 'ROLE_ADMIN';
//        """,
//      nativeQuery = true)
//  long countNewMembersOfRecentOneMonthWithoutAdmin();
//
//
//  @Query(value = """
//      SELECT count(m.member_id) AS total_user_members_count
//      FROM member m
//               JOIN member_role mr ON m.member_id = mr.member_id
//               JOIN role r ON mr.role_id = r.role_id
//      WHERE r.name != 'ROLE_ADMIN';""",
//      nativeQuery = true)
//  long countAllMembersWithoutAdmin();



}
