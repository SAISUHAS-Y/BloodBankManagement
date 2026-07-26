package com.bloodbank.master.domain.repository;

import com.bloodbank.master.domain.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByStateIdAndActiveTrueOrderByNameAsc(Long stateId);
    List<District> findByStateIdOrderByNameAsc(Long stateId);
    List<District> findByStateId(Long stateId);
}
