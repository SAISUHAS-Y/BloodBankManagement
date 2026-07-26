package com.bloodbank.user.application.event;

import com.bloodbank.common.events.donor.DonorRegisteredEvent;
import com.bloodbank.common.events.outbox.EventPublisher;
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
}
