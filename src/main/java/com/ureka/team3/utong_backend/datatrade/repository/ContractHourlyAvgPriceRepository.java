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
}
