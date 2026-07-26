package com.bloodbank.bloodbank.domain.repository;

import com.bloodbank.bloodbank.domain.entity.ProcessedStockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedStockTransactionRepository extends JpaRepository<ProcessedStockTransaction, String> {
}
