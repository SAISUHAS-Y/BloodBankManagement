package com.bloodbank.user.domain.repository;

import com.bloodbank.user.domain.entity.ProcessedDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedDonationRepository extends JpaRepository<ProcessedDonation, Long> {
}
