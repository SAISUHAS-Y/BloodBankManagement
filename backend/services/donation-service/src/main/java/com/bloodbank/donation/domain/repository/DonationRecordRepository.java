package com.bloodbank.donation.domain.repository;

import com.bloodbank.donation.domain.entity.DonationRecord;
import com.bloodbank.donation.domain.enums.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {

    List<DonationRecord> findByDonorProfileIdOrderByDonationDateDesc(Long donorProfileId);

    List<DonationRecord> findByDonationStatusAndUpdatedAtBefore(DonationStatus status, Instant threshold);

    @Query("SELECT d FROM DonationRecord d WHERE " +
           "(:donorProfileId IS NULL OR d.donorProfileId = :donorProfileId) AND " +
           "(:bloodBankId IS NULL OR d.bloodBankId = :bloodBankId) AND " +
           "(:status IS NULL OR d.donationStatus = :status)")
    Page<DonationRecord> searchDonations(
            @Param("donorProfileId") Long donorProfileId,
            @Param("bloodBankId") Long bloodBankId,
            @Param("status") DonationStatus status,
            Pageable pageable);
}
