package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.entity.ContractHourlyAvgPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractHourlyAvgPriceRepository extends JpaRepository<ContractHourlyAvgPrice, String> {

    // 집계 데이터 조회
    @Query(value = """
        SELECT * FROM contract_hourly_avg_price
        WHERE data_code = :dataCode
        AND aggregated_at <= :aggregatedAt
        ORDER BY aggregated_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<ContractHourlyAvgPrice> findLatestByDataCodeBeforeTime(
            @Param("dataCode") String dataCode,
            @Param("aggregatedAt") LocalDateTime aggregatedAt,
            @Param("limit") int limit
    );

    // 주간 시세 조회를 위한 일별 평균 계산
    @Query(value = """
        SELECT 
            DATE(aggregated_at) as date,
            AVG(avg_price) as avg_price,
            data_code
        FROM contract_hourly_avg_price
        WHERE data_code = :dataCode
        AND DATE(aggregated_at) >= DATE_SUB(CURDATE(), INTERVAL 8 DAY)
        AND DATE(aggregated_at) <= DATE_SUB(CURDATE(), INTERVAL 1 DAY)
        GROUP BY DATE(aggregated_at), data_code
        ORDER BY DATE(aggregated_at) DESC
    """, nativeQuery = true)
    List<Object[]> findWeeklyAvgPricesByDataCode(@Param("dataCode") String dataCode);
}
