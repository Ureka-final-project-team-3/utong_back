package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleDataRequestRepository extends JpaRepository<SaleDataRequest, String> {
    @Query("SELECT COUNT(sdr) > 0 FROM SaleDataRequest sdr WHERE sdr.lineId = :lineId AND (sdr.status = '002' or sdr.status='003')")
    boolean existsWaitingRequestByLineId(@Param("lineId") String id);

    // 본인 판매 대기 내역 조회
    @Query("""
        SELECT s FROM SaleDataRequest s
        WHERE s.account.id = :accountId
         AND (s.status = '002' OR s.status = '003')
        AND s.createdAt >= :fromDate
        ORDER BY s.createdAt DESC
    """)
    List<SaleDataRequest> findWaitingSalesByAccountId(
            @Param("accountId") String accountId,
            @Param("fromDate") LocalDateTime fromDate
    );
}

