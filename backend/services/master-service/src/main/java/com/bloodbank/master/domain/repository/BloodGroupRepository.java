package com.bloodbank.master.domain.repository;

import com.bloodbank.master.domain.entity.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {
    Optional<BloodGroup> findByCode(String code);
}
