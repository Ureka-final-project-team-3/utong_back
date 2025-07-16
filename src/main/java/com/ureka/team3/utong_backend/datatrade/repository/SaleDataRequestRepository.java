package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleDataRequestRepository extends JpaRepository<SaleDataRequest, String> {
    @Query("SELECT COUNT(sdr) > 0 FROM SaleDataRequest sdr WHERE sdr.lineId = :lineId AND (sdr.status = '002' or sdr.status='003')")
    boolean existsWaitingRequestByLineId(@Param("lineId") String id);
}
