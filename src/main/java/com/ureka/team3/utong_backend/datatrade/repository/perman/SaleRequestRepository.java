package com.ureka.team3.utong_backend.datatrade.repository.perman;

import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRequestRepository extends JpaRepository<SaleDataRequest, String> {
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

    // 본인 판매 대기 내역 조회
    @Query("""
                SELECT s FROM SaleDataRequest s
                WHERE s.account.id = :accountId
                AND s.createdAt >= :fromDate
                ORDER BY s.createdAt DESC
            """)
    List<SaleDataRequest> findSaleRequestsByAccountId(
            @Param("accountId") String accountId,
            @Param("fromDate") LocalDateTime fromDate
    );


}

