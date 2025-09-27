package com.project.maumii_be.repository;

import com.project.maumii_be.domain.Bubble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ChartReportRepository extends JpaRepository<Bubble, Long> {
    // 긍정 감정이 많은 상대 내림차순 정렬
    @Query(value = "select  DATE(r.r_created_at) as date,\n" +
           "    SUM(CASE WHEN b.b_emotion='happy' THEN 1 ELSE 0 END) as happy_count,\n" +
           "    SUM(CASE WHEN b.b_emotion IN ('sad','scared') THEN 1 ELSE 0 END) as sad_scared_count,\n" +
           "    SUM(CASE WHEN b.b_emotion IN ('angry','disgust') THEN 1 ELSE 0 END) as angry_disgust_count,\n" +
           "    SUM(CASE WHEN b.b_emotion NOT IN ('happy','sad','scared','angry','disgust') OR b.b_emotion IS NULL THEN 1 ELSE 0 END) as other_count\n" +
           "from bubble b right join record r on b.r_id = r.r_id\n" +
           "right join record_list rl on r.rl_id = rl.rl_id\n" +
           "right join users u on rl.u_id = u.u_id\n" +
           "where u.u_id = :userId and YEAR(r.r_created_at) = :inputYear and MONTH(r.r_created_at) = :inputMonth\n" +
           "group by DATE(r.r_created_at)\n" +
           "order by date", nativeQuery = true)
    List<Object[]> findEmotionGroupFlow(@Param("userId") String userId,
                                        @Param("inputYear") int inputYear,
                                        @Param("inputMonth") int inputMonth);

    // 감정 캘린터
    // 부정 감정이 많은 상대 내림차순 정렬
    // 긍정, 부정1, 부정2 감정의 추이 (시간의 흐름) 조회
}