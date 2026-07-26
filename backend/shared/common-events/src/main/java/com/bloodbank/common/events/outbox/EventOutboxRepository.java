package com.bloodbank.common.events.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventOutboxRepository extends JpaRepository<EventOutbox, Long> {
    List<EventOutbox> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
