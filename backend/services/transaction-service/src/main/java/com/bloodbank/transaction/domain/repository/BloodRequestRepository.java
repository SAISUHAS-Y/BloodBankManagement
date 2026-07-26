package com.bloodbank.transaction.domain.repository;

import com.bloodbank.transaction.domain.entity.BloodRequest;
import com.bloodbank.transaction.domain.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByHospitalIdOrderByRequestedAtDesc(Long hospitalId);
    boolean existsByHospitalId(Long hospitalId);

    @Query("SELECT r FROM BloodRequest r WHERE " +
           "(:hospitalId IS NULL OR r.hospitalId = :hospitalId) AND " +
           "(:bloodGroupId IS NULL OR r.bloodGroupId = :bloodGroupId) AND " +
           "(:status IS NULL OR r.requestStatus = :status) " +
           "ORDER BY CASE r.urgency WHEN com.bloodbank.transaction.domain.enums.Urgency.EMERGENCY THEN 1 " +
           "WHEN com.bloodbank.transaction.domain.enums.Urgency.URGENT THEN 2 " +
           "WHEN com.bloodbank.transaction.domain.enums.Urgency.ROUTINE THEN 3 ELSE 4 END ASC, " +
           "r.requestedAt DESC")
    Page<BloodRequest> searchRequests(
            @Param("hospitalId") Long hospitalId,
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("status") RequestStatus status,
            Pageable pageable);
}
