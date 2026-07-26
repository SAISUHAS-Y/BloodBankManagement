package com.bloodbank.hospital.domain.repository;

import com.bloodbank.hospital.domain.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    Optional<Hospital> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT h FROM Hospital h WHERE " +
           "(:stateId IS NULL OR h.stateId = :stateId) AND " +
           "(:districtId IS NULL OR h.districtId = :districtId) AND " +
           "(:cityId IS NULL OR h.cityId = :cityId) AND " +
           "(:isActive IS NULL OR h.isActive = :isActive)")
    Page<Hospital> searchHospitals(
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("cityId") Long cityId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
