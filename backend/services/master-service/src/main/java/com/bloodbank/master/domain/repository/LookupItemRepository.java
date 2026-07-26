package com.bloodbank.master.domain.repository;

import com.bloodbank.master.domain.entity.LookupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupItemRepository extends JpaRepository<LookupItem, Long> {
    List<LookupItem> findByCategoryCodeOrderBySortOrderAsc(String categoryCode);
    List<LookupItem> findByCategoryCodeAndActiveTrueOrderBySortOrderAsc(String categoryCode);
    Optional<LookupItem> findByCategoryCodeAndCode(String categoryCode, String code);
    List<LookupItem> findByCategoryId(Long categoryId);
    List<LookupItem> findByActiveTrueOrderBySortOrderAsc();
}
