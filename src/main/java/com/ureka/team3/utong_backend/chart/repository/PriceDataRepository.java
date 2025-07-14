package com.ureka.team3.utong_backend.chart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ureka.team3.utong_backend.chart.entity.Contract;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceDataRepository extends JpaRepository<Contract, String> {
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(c.created_at, '%Y-%m-%dT%H:%i:00') as timestamp,
            sdr.price as price,
            COUNT(*) as volume
        FROM contract c
        JOIN sale_data_request sdr ON c.sale_data_request_id = sdr.id
        WHERE c.status_code = '002' 
        AND sdr.data_code = :dataCode
        AND c.created_at >= :startTime
        GROUP BY DATE_FORMAT(c.created_at, '%Y-%m-%dT%H:%i:00'), sdr.price
        ORDER BY timestamp DESC
        LIMIT 20
        """, nativeQuery = true)
    List<Object[]> findRecentPriceData(@Param("dataCode") String dataCode, 
                                       @Param("startTime") LocalDateTime startTime);
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(c.created_at, '%Y-%m-%dT%H:%i:%s') as timestamp,
            sdr.price as price,
            sdr.quantity as volume
        FROM contract c
        JOIN sale_data_request sdr ON c.sale_data_request_id = sdr.id
        WHERE c.status_code = '002' 
        AND sdr.data_code = :dataCode
        AND c.id = :contractId
        """, nativeQuery = true)
    Object[] findLatestTradeData(@Param("dataCode") String dataCode, 
                                 @Param("contractId") String contractId);
}