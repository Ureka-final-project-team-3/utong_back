package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BuyDataRequestRepository extends JpaRepository<BuyDataRequest, String> {
    @Query("SELECT COUNT(bdr) > 0 FROM BuyDataRequest bdr WHERE bdr.lineId = :lineId AND (bdr.status = '002' or bdr.status='003')")
    boolean existsWaitingRequestByLineId(@Param("lineId") String id);

    // 본인 구매 대기 내역 조회
    @Query("""
        SELECT b FROM BuyDataRequest b
        WHERE b.account.id = :accountId
        AND (b.status = '002' OR b.status = '003')
        AND b.createdAt >= :fromDate
        ORDER BY b.createdAt DESC
    """)
    List<BuyDataRequest> findWaitingPurchasesByAccountId(
            @Param("accountId") String accountId,
            @Param("fromDate") LocalDateTime fromDate
    );
}
