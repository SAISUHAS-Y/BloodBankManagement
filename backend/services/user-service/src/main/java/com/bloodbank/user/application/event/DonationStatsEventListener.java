package com.bloodbank.user.application.event;

import com.bloodbank.common.contracts.dto.RecordDonationRequest;
import com.bloodbank.common.events.EventEnvelope;
import com.bloodbank.common.events.donation.DonationStatsUpdateRequiredEvent;
import com.bloodbank.user.application.service.DonorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DonationStatsEventListener {

    private final DonorProfileService donorProfileService;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(name = "user.donation.stats.update.queue", durable = "true"),
            exchange = @Exchange(name = "donation.exchange", type = "topic", durable = "true"),
            key = "donation.stats.update"
        )
    )
    public void handleDonationStatsUpdate(EventEnvelope<DonationStatsUpdateRequiredEvent> envelope) {
        DonationStatsUpdateRequiredEvent payload = envelope.getPayload();
        log.info("Received DonationStatsUpdateRequiredEvent: donationId={} donorId={}",
                payload.getDonationId(), payload.getDonorProfileId());

        RecordDonationRequest request = RecordDonationRequest.builder()
                .donationId(payload.getDonationId())
                .donationDate(payload.getDonationDate())
                .unitsCollected(payload.getUnitsCollected())
                .build();

        try {
            donorProfileService.recordDonation(payload.getDonorProfileId(), request);
            log.info("Successfully updated donor stats asynchronously for donor ID: {}", payload.getDonorProfileId());
        } catch (Exception e) {
            log.error("Failed to update donor stats asynchronously for donor ID: {}", payload.getDonorProfileId(), e);
            throw e; // Trigger RabbitMQ retry / dead-letter queue
        }
    }
}
