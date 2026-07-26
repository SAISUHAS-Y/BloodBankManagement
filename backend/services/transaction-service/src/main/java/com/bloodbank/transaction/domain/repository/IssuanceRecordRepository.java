package com.bloodbank.transaction.domain.repository;

import com.bloodbank.transaction.domain.entity.IssuanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssuanceRecordRepository extends JpaRepository<IssuanceRecord, Long> {
    List<IssuanceRecord> findByBloodRequestId(Long bloodRequestId);
}
