package com.bloodbank.common.events.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollerScheduler {

    private final EventOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.poller.rate-ms:3000}")
    @Transactional
    public void processOutboxEvents() {
        List<EventOutbox> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("[OUTBOX POLLER] Processing {} pending outbox records", pendingEvents.size());

        for (EventOutbox outbox : pendingEvents) {
            try {
                rabbitTemplate.convertAndSend(outbox.getExchange(), outbox.getRoutingKey(), outbox.getPayload());
                outbox.setStatus(OutboxStatus.PUBLISHED);
                outbox.setPublishedAt(Instant.now());
                log.info("[OUTBOX PUBLISHED] Successfully dispatched eventId: {} to exchange: {}", outbox.getEventId(), outbox.getExchange());
            } catch (Exception ex) {
                int retries = outbox.getRetryCount() + 1;
                outbox.setRetryCount(retries);
                outbox.setErrorMessage(ex.getMessage() != null ? ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 500)) : "Publish failed");

                if (retries >= 5) {
                    outbox.setStatus(OutboxStatus.FAILED);
                    log.error("[OUTBOX FAILED] EventId: {} failed after {} retries", outbox.getEventId(), retries, ex);
                } else {
                    log.warn("[OUTBOX RETRY] EventId: {} publish failed (Attempt {}/5). Error: {}", outbox.getEventId(), retries, ex.getMessage());
                }
            }
            outboxRepository.save(outbox);
        }
    }
}
