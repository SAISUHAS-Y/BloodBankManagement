package com.bloodbank.notification.domain.repository;

import com.bloodbank.notification.domain.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    
    boolean existsByEventId(String eventId);

    Optional<NotificationLog> findByEventId(String eventId);

    @Query("""
        SELECT n FROM NotificationLog n
        WHERE (:recipientId IS NULL OR n.recipientId = :recipientId)
          AND (:eventType IS NULL OR n.eventType = :eventType)
          AND (:status IS NULL OR n.status = :status)
          AND (:startDate IS NULL OR n.sentAt >= :startDate)
          AND (:endDate IS NULL OR n.sentAt <= :endDate)
          AND n.isDeleted = false
        ORDER BY n.sentAt DESC
    """)
    Page<NotificationLog> findByFilters(
            @Param("recipientId") String recipientId,
            @Param("eventType") String eventType,
            @Param("status") String status,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
