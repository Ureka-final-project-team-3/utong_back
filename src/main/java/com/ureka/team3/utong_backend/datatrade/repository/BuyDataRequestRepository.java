package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyDataRequestRepository extends JpaRepository<BuyDataRequest, String> {
    @Query("SELECT COUNT(bdr) > 0 FROM BuyDataRequest bdr WHERE bdr.lineId = :lineId AND (bdr.status = '002' or bdr.status='003')")
    boolean existsWaitingRequestByLineId(@Param("lineId") String id);
}
