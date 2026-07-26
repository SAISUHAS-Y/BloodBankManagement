package com.bloodbank.master.domain.repository;

import com.bloodbank.master.domain.entity.LookupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LookupCategoryRepository extends JpaRepository<LookupCategory, Long> {
    Optional<LookupCategory> findByCode(String code);
    boolean existsByCode(String code);
}
