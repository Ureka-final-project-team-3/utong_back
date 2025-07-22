package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.entity.Contract;
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
    """)
    List<Contract> findCompletedSalesByAccountId(
            @Param("accountId") String accountId,
            @Param("fromDate") LocalDateTime fromDate
    );

    @Query(value = """
       SELECT c.* FROM contract c
       INNER JOIN buy_data_request bdr ON c.buy_data_request_id = bdr.id
       WHERE bdr.data_code = :dataCode
       ORDER BY c.created_at DESC
       LIMIT :limit
    """, nativeQuery = true)
    List<Contract> findLatestContractByDataCode(@Param("dataCode") String dataCode, @Param("limit") int limit);
}
