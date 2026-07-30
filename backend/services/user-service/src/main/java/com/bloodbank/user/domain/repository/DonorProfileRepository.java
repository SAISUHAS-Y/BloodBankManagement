package com.bloodbank.user.domain.repository;

import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long>, JpaSpecificationExecutor<DonorProfile> {

    Optional<DonorProfile> findByIdentityUserId(Long identityUserId);
    Optional<DonorProfile> findByPhone(String phone);

    @Query("SELECT d FROM DonorProfile d WHERE " +
           "(:bloodGroupId IS NULL OR d.bloodGroupId = :bloodGroupId) AND " +
           "(:stateId IS NULL OR d.stateId = :stateId) AND " +
           "(:districtId IS NULL OR d.districtId = :districtId) AND " +
           "(:cityId IS NULL OR d.cityId = :cityId) AND " +
           "(:status IS NULL OR d.donorStatus = :status)")
    Page<DonorProfile> searchDonors(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("cityId") Long cityId,
            @Param("status") DonorStatus status,
            Pageable pageable);
}
