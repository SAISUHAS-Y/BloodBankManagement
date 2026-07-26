package com.bloodbank.bloodbank.domain.repository;

import com.bloodbank.bloodbank.domain.entity.BloodBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {

    Optional<BloodBank> findByLicenseNumber(String licenseNumber);

    @Query("SELECT b FROM BloodBank b WHERE " +
           "(:stateId IS NULL OR b.stateId = :stateId) AND " +
           "(:districtId IS NULL OR b.districtId = :districtId) AND " +
           "(:cityId IS NULL OR b.cityId = :cityId) AND " +
           "(:isActive IS NULL OR b.isActive = :isActive)")
    Page<BloodBank> searchBloodBanks(
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("cityId") Long cityId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
