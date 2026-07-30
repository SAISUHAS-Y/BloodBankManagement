package com.bloodbank.user.domain.repository;

import com.bloodbank.user.domain.entity.ProfileAuditHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileAuditHistoryRepository extends JpaRepository<ProfileAuditHistory, Long> {
    List<ProfileAuditHistory> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);
    Page<ProfileAuditHistory> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
}
