package com.bloodbank.bloodbank.application.listener;

import com.bloodbank.common.events.EventEnvelope;
import com.bloodbank.common.events.donation.DonationCompletedEvent;
import com.bloodbank.bloodbank.application.config.RabbitConfig;
import com.bloodbank.bloodbank.application.service.BloodStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DonationCompletedEventListener {

    private final BloodStockService bloodStockService;

    @RabbitListener(queues = RabbitConfig.DONATION_COMPLETED_QUEUE)
    public void onDonationCompleted(EventEnvelope<DonationCompletedEvent> envelope) {
        log.info("Received DonationCompletedEvent envelope: traceId={}", envelope.getTraceId());
        
        DonationCompletedEvent event = envelope.getPayload();
        if (event == null) {
            log.error("Received null payload in DonationCompletedEvent");
            return;
        }

        try {
            bloodStockService.handleDonationCompleted(
                    event.getBloodBankId(),
                    event.getBloodGroupCode(),
                    event.getComponentTypeCode(),
                    event.getUnits(),
                    envelope.getTraceId()
            );
            log.info("Successfully updated stock for blood bank ID: {} from donation ID: {}", 
                    event.getBloodBankId(), event.getDonationId());
        } catch (Exception e) {
            log.error("Failed to process DonationCompletedEvent for donation ID: {}", event.getDonationId(), e);
        }
    }
}
