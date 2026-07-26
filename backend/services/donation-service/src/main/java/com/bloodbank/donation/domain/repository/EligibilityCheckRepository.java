package com.bloodbank.donation.domain.repository;

import com.bloodbank.donation.domain.entity.EligibilityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EligibilityCheckRepository extends JpaRepository<EligibilityCheck, Long> {
    List<EligibilityCheck> findByDonorProfileIdOrderByCheckedAtDesc(Long donorProfileId);
}
