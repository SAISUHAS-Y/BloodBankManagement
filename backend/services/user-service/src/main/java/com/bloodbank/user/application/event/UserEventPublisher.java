package com.bloodbank.user.application.event;

import com.bloodbank.common.events.donor.DonorRegisteredEvent;
import com.bloodbank.common.events.donor.DonorStatusChangedEvent;
import com.bloodbank.common.events.outbox.EventPublisher;
import com.bloodbank.common.events.staff.StaffStatusChangedEvent;
import com.bloodbank.user.application.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final EventPublisher eventPublisher;

    public void publishDonorRegistered(Long donorId, String firstName, String lastName, String email, String bloodType, String traceId) {
        DonorRegisteredEvent payload = DonorRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .donorId(donorId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .bloodType(bloodType)
                .build();

        String routingKey = "donor.registered";
        log.info("[USER SERVICE] Staging DonorRegisteredEvent: exchange={} routingKey={} donorId={}",
                RabbitConfig.DONOR_EXCHANGE, routingKey, donorId);

        eventPublisher.publish(RabbitConfig.DONOR_EXCHANGE, routingKey, payload);
    }

    public void publishDonorStatusChanged(Long donorId, String oldStatus, String newStatus, String reason, String changedBy) {
        DonorStatusChangedEvent payload = DonorStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .donorId(donorId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(changedBy)
                .build();

        String routingKey = "donor.status.changed";
        log.info("[USER SERVICE] Staging DonorStatusChangedEvent: exchange={} routingKey={} donorId={} newStatus={}",
                RabbitConfig.DONOR_EXCHANGE, routingKey, donorId, newStatus);

        eventPublisher.publish(RabbitConfig.DONOR_EXCHANGE, routingKey, payload);
    }

    public void publishStaffStatusChanged(Long staffId, String oldStatus, String newStatus, String reason, String changedBy) {
        StaffStatusChangedEvent payload = StaffStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .staffId(staffId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(changedBy)
                .build();

        String routingKey = "staff.status.changed";
        log.info("[USER SERVICE] Staging StaffStatusChangedEvent: exchange={} routingKey={} staffId={} newStatus={}",
                RabbitConfig.STAFF_EXCHANGE, routingKey, staffId, newStatus);

        eventPublisher.publish(RabbitConfig.STAFF_EXCHANGE, routingKey, payload);
    }
}
