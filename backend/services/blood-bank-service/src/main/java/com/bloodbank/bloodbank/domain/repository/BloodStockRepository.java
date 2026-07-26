package com.bloodbank.bloodbank.domain.repository;

import com.bloodbank.bloodbank.domain.entity.BloodStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodStockRepository extends JpaRepository<BloodStock, Long> {

    Optional<BloodStock> findByBloodBankIdAndBloodGroupIdAndComponentTypeCode(
            Long bloodBankId, Long bloodGroupId, String componentTypeCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BloodStock s WHERE s.id = :id")
    Optional<BloodStock> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BloodStock s WHERE s.bloodBank.id = :bloodBankId AND s.bloodGroupId = :bloodGroupId AND s.componentTypeCode = :componentTypeCode")
    Optional<BloodStock> findForUpdate(
            @Param("bloodBankId") Long bloodBankId,
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("componentTypeCode") String componentTypeCode);

    List<BloodStock> findByBloodBankId(Long bloodBankId);

    @Query("SELECT s FROM BloodStock s JOIN s.bloodBank b WHERE " +
           "s.bloodGroupId = :bloodGroupId AND " +
           "(:stateId IS NULL OR b.stateId = :stateId) AND " +
           "(:districtId IS NULL OR b.districtId = :districtId) AND " +
           "s.unitsAvailable >= :minUnits AND " +
           "b.isActive = TRUE")
    List<BloodStock> searchAvailableStock(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("minUnits") double minUnits);
}
