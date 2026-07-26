package com.bloodbank.notification.domain.repository;

import com.bloodbank.notification.domain.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByCodeAndIsDeletedFalse(String code);
    boolean existsByCodeAndIsDeletedFalse(String code);
}
