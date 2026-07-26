package com.bloodbank.bloodbank.domain.repository;

import com.bloodbank.bloodbank.domain.entity.StockAdjustmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentLogRepository extends JpaRepository<StockAdjustmentLog, Long> {
    List<StockAdjustmentLog> findByBloodStockIdOrderByAdjustedAtDesc(Long bloodStockId);
}
