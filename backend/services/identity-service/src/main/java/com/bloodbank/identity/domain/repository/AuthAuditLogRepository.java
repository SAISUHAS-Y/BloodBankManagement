package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.AuthAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {
    List<AuthAuditLog> findByUserId(Long userId);
}
