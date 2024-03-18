package com.study.poetry.repository;

import com.study.poetry.entity.Member;
import com.study.poetry.entity.Poem;
import com.study.poetry.dto.admin.PoemStatisticsInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PoemRepository extends JpaRepository<Poem, Long> {
  //category, keword, type
  Page<Poem> findAllByCategoryIdAndDeletedIsFalseAndContentContainingIgnoreCaseOrderByWriteDateDesc(Integer categoryId, String keyword, PageRequest pageRequest);

  Page<Poem> findAllByCategoryIdAndDeletedIsFalseAndTitleContainingIgnoreCaseOrderByWriteDateDesc(Integer categoryId, String keyword, PageRequest pageRequest);

  Page<Poem> findByCategoryIdAndDeletedIsFalseAndMember_NameContainingIgnoreCaseOrderByWriteDateDesc(Integer categoryId, String keyword, PageRequest pageRequest);

  Page<Poem> findByMember_MemberIdAndDeletedIsFalseOrderByWriteDateDesc(Long memberId, PageRequest pageRequest);

  void deleteAllByMember(Member member);

  List<Poem> findByMember(Member member);

  @Query(value = """
      SELECT p.*,
             count(DISTINCT b.bookmark_id) AS bookmark_count,
             count(DISTINCT pvl.id)        AS views_count
      FROM poem p
               JOIN member m ON p.member_id = m.member_id
               JOIN poem_settings ps ON p.poem_settings_id = ps.poem_settings_id
               LEFT JOIN poem_view_log pvl ON p.poem_id = pvl.poem_id
               LEFT JOIN bookmark b ON p.poem_id = b.poem_id
      WHERE date_format(p.write_date,'%Y-%m') = date_format(curdate(), '%Y-%m')
      GROUP BY  p.poem_id
      ORDER BY bookmark_count DESC , views_count DESC
      LIMIT 1
                 """,
      nativeQuery = true)
  Optional<Poem> selectMostViewedPoemThisMonth();

  @Query(value =
      """
          SELECT p.*,
                 count(DISTINCT b.bookmark_id) AS bookmark_count,
                 count(DISTINCT pvl.id)        AS views_count
          FROM poem p
                   JOIN member m ON p.member_id = m.member_id
                   JOIN poem_settings ps ON p.poem_settings_id = ps.poem_settings_id
                   LEFT JOIN poem_view_log pvl ON p.poem_id = pvl.poem_id
                   LEFT JOIN bookmark b ON p.poem_id = b.poem_id
          GROUP BY  p.poem_id
          ORDER BY bookmark_count DESC , views_count DESC
          LIMIT 1""",
      nativeQuery = true)
  Optional<Poem> selectMostViedPoemAllTime();

  @Query(value = """
      SELECT
          p.category_id as categoryId,
          c.category_name AS categoryName,
          COUNT(*) AS totalPoemsCount,
          SUM(CASE WHEN DATEDIFF(CURDATE(), write_date) = 0 THEN 1 ELSE 0 END) AS poemsTodayCount,
          SUM(CASE WHEN write_date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) THEN 1 ELSE 0 END) AS poemsLastMonthCount,
          SUM(CASE WHEN write_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH) THEN 1 ELSE 0 END) AS poemsLast3MonthsCount,
          SUM(CASE WHEN write_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) THEN 1 ELSE 0 END) AS poemsLast6MonthsCount,
          SUM(CASE WHEN write_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) THEN 1 ELSE 0 END) AS poemsLastYearCount
      FROM
          poem p
      JOIN category c On c.category_id = p.category_id
      GROUP BY
          p.category_id,c.category_order
      ORDER BY c.category_order""", nativeQuery = true)
  List<PoemStatisticsInterface> selectPoemStatistics();
}
