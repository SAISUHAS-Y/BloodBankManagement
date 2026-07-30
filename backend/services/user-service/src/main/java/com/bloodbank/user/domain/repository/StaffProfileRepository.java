package com.bloodbank.user.domain.repository;

import com.bloodbank.user.domain.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long>, JpaSpecificationExecutor<StaffProfile> {
    Optional<StaffProfile> findByIdentityUserId(Long identityUserId);
    List<StaffProfile> findByReportingManagerId(Long reportingManagerId);
}
