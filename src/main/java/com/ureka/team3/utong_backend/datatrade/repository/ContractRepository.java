package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.domain.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {

    // 구매 완료
    @Query("""
        SELECT c FROM Contract c
        WHERE c.buyDataRequest.account.id = :accountId
        AND c.createdAt >= :fromDate
        ORDER BY c.createdAt DESC
    """)
    List<Contract> findCompletedPurchasesByAccountId(
            @Param("accountId") String accountId,
            @Param("fromDate") LocalDateTime fromDate
    );

    // 판매 완료
    @Query("""
        SELECT c FROM Contract c
        WHERE c.saleDataRequest.account.id = :accountId
        AND c.createdAt >= :fromDate
        ORDER BY c.createdAt DESC
    """)
    List<Contract> findCompletedSalesByAccountId(
            @Param("accountId") String accountId,
            @Param("fromDate") LocalDateTime fromDate
    );


    // 최근 N 일간의 일별 평균 가격을 조회하는 메서드
    @Query(value = """
        SELECT 
            DATE(c.created_at) as date,
            COALESCE(
                SUM(c.price * c.amount) / NULLIF(SUM(c.amount), 0), 
                0
            ) as avgPrice
        FROM contract c
        INNER JOIN buy_data_request bdr ON c.buy_data_request_id = bdr.id
        WHERE c.created_at >= :startDate
        AND c.created_at < :endDate
        AND bdr.data_code = :dataCode
        GROUP BY DATE(c.created_at)
        ORDER BY DATE(c.created_at) ASC
    """, nativeQuery = true)
    List<Object[]> findDailyAvgPricesForLastDays(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("dataCode") String dataCode
    );

    @Query(value = """
        SELECT 
            COALESCE(
                SUM(c.price * c.amount) / NULLIF(SUM(c.amount), 0), 
                0
            ) as avgPrice
        FROM contract c
        INNER JOIN buy_data_request bdr ON c.buy_data_request_id = bdr.id
        WHERE c.created_at < :beforeDate
        AND bdr.data_code = :dataCode
        ORDER BY c.created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Long findLatestAvgPriceBeforeDate(
            @Param("beforeDate") LocalDateTime beforeDate,
            @Param("dataCode") String dataCode
    );
}
