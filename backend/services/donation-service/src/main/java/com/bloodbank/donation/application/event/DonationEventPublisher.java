package com.bloodbank.donation.application.event;

import com.bloodbank.common.events.donation.DonationCompletedEvent;
import com.bloodbank.common.events.donation.DonationStatsUpdateRequiredEvent;
import com.bloodbank.common.events.outbox.EventPublisher;
import com.bloodbank.donation.application.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DonationEventPublisher {

    private final EventPublisher eventPublisher;

    public void publishDonationCompleted(
            Long donationId, Long donorId, Long bloodBankId, 
            String bloodGroupCode, String componentTypeCode, double units, String traceId) {
        
        DonationCompletedEvent payload = DonationCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .donationId(donationId)
                .donorId(donorId)
                .bloodBankId(bloodBankId)
                .bloodGroupCode(bloodGroupCode)
                .componentTypeCode(componentTypeCode)
                .units(units)
                .build();

        String routingKey = "donation.completed";
        log.info("[DONATION SERVICE] Staging DonationCompletedEvent: exchange={} routingKey={} donationId={}",
                RabbitConfig.DONATION_EXCHANGE, routingKey, donationId);

        eventPublisher.publish(RabbitConfig.DONATION_EXCHANGE, routingKey, payload);
    }

    public void publishDonationStatsUpdateRequired(
            Long donationId, Long donorId, LocalDate donationDate, double units, String traceId) {
        
        DonationStatsUpdateRequiredEvent payload = DonationStatsUpdateRequiredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .donationId(donationId)
                .donorProfileId(donorId)
                .donationDate(donationDate)
                .unitsCollected(units)
                .build();

        String routingKey = "donation.stats.update";
        log.info("[DONATION SERVICE] Staging DonationStatsUpdateRequiredEvent: exchange={} routingKey={} donationId={}",
                RabbitConfig.DONATION_EXCHANGE, routingKey, donationId);

        eventPublisher.publish(RabbitConfig.DONATION_EXCHANGE, routingKey, payload);
    }
}
