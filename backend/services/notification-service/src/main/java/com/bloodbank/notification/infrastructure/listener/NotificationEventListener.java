package com.bloodbank.notification.infrastructure.listener;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bloodbank.common.events.DomainEvent;
import com.bloodbank.common.events.EventEnvelope;
import com.bloodbank.common.events.donation.DonationCompletedEvent;
import com.bloodbank.common.events.donor.DonorRegisteredEvent;
import com.bloodbank.common.events.master.LookupItemChangedEvent;
import com.bloodbank.common.events.transaction.BloodRequestCreatedEvent;
import com.bloodbank.notification.application.sender.EmailSender;
import com.bloodbank.notification.application.sender.SmsSender;
import com.bloodbank.notification.application.service.NotificationTemplateService;
import com.bloodbank.notification.domain.entity.NotificationLog;
import com.bloodbank.notification.domain.entity.NotificationTemplate;
import com.bloodbank.notification.domain.repository.NotificationLogRepository;
import com.bloodbank.notification.infrastructure.config.NotificationRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationLogRepository logRepository;
    private final NotificationTemplateService templateService;
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final ObjectMapper objectMapper;

    @RabbitListener(
        queues = NotificationRabbitConfig.DONOR_REGISTERED_QUEUE,
        containerFactory = "userNotificationListenerContainerFactory"
    )
    @Transactional
    public void handleDonorRegistered(Object rawMessage) {
        EventEnvelope<DonorRegisteredEvent> envelope = convertEnvelope(rawMessage, DonorRegisteredEvent.class);
        String eventId = envelope.getEventId();
        DonorRegisteredEvent event = envelope.getPayload();

        log.info("Received DonorRegisteredEvent [eventId={}, donorId={}]", eventId, event.getDonorId());

        if (logRepository.existsByEventId(eventId)) {
            log.warn("Idempotency check triggered: Event [eventId={}] already processed. Skipping.", eventId);
            return;
        }

        try {
            NotificationTemplate template = templateService.getTemplateByCode("DONOR_WELCOME");
            Map<String, Object> vars = Map.of(
                    "firstName", event.getFirstName() != null ? event.getFirstName() : "Valued",
                    "lastName", event.getLastName() != null ? event.getLastName() : "Donor",
                    "bloodType", event.getBloodType() != null ? event.getBloodType() : "Unknown"
            );

            String body = templateService.render(template.getBodyTemplate(), vars);
            String subject = templateService.render(template.getSubject(), vars);

            emailSender.sendEmail(event.getEmail() != null ? event.getEmail() : "donor@bloodbank.com", subject, body);

            saveLog(eventId, "DonorRegisteredEvent", "DONOR", String.valueOf(event.getDonorId()),
                    "EMAIL", "DONOR_WELCOME", "SENT", null);

        } catch (Exception ex) {
            log.error("Failed to process DonorRegisteredEvent [eventId={}]", eventId, ex);
            saveLog(eventId, "DonorRegisteredEvent", "DONOR", String.valueOf(event.getDonorId()),
                    "EMAIL", "DONOR_WELCOME", "FAILED", ex.getMessage());
            throw ex;
        }
    }

    @RabbitListener(
        queues = NotificationRabbitConfig.DONATION_COMPLETED_QUEUE,
        containerFactory = "userNotificationListenerContainerFactory"
    )
    @Transactional
    public void handleDonationCompleted(Object rawMessage) {
        EventEnvelope<DonationCompletedEvent> envelope = convertEnvelope(rawMessage, DonationCompletedEvent.class);
        String eventId = envelope.getEventId();
        DonationCompletedEvent event = envelope.getPayload();

        log.info("Received DonationCompletedEvent [eventId={}, donationId={}]", eventId, event.getDonationId());

        if (logRepository.existsByEventId(eventId)) {
            log.warn("Idempotency check triggered: Event [eventId={}] already processed. Skipping.", eventId);
            return;
        }

        try {
            NotificationTemplate donorTemplate = templateService.getTemplateByCode("DONATION_THANKYOU");
            Map<String, Object> vars = Map.of(
                    "donationId", event.getDonationId(),
                    "units", event.getUnits(),
                    "componentType", event.getComponentTypeCode() != null ? event.getComponentTypeCode() : "Whole Blood"
            );

            String donorBody = templateService.render(donorTemplate.getBodyTemplate(), vars);
            String donorSubject = templateService.render(donorTemplate.getSubject(), vars);

            emailSender.sendEmail("donor-" + event.getDonorId() + "@bloodbank.com", donorSubject, donorBody);

            NotificationTemplate staffTemplate = templateService.getTemplateByCode("STAFF_UNIT_AVAILABLE");
            Map<String, Object> staffVars = Map.of(
                    "bloodBankId", event.getBloodBankId(),
                    "donationId", event.getDonationId(),
                    "bloodGroup", event.getBloodGroupCode() != null ? event.getBloodGroupCode() : "N/A",
                    "units", event.getUnits()
            );

            String staffBody = templateService.render(staffTemplate.getBodyTemplate(), staffVars);
            String staffSubject = templateService.render(staffTemplate.getSubject(), staffVars);

            emailSender.sendEmail("staff-bank-" + event.getBloodBankId() + "@bloodbank.com", staffSubject, staffBody);

            saveLog(eventId, "DonationCompletedEvent", "DONOR", String.valueOf(event.getDonorId()),
                    "EMAIL", "DONATION_THANKYOU", "SENT", null);

        } catch (Exception ex) {
            log.error("Failed to process DonationCompletedEvent [eventId={}]", eventId, ex);
            saveLog(eventId, "DonationCompletedEvent", "DONOR", String.valueOf(event.getDonorId()),
                    "EMAIL", "DONATION_THANKYOU", "FAILED", ex.getMessage());
            throw ex;
        }
    }

    @RabbitListener(
        queues = NotificationRabbitConfig.BLOOD_REQUEST_QUEUE,
        containerFactory = "emergencyAlertListenerContainerFactory"
    )
    @Transactional
    public void handleBloodRequestCreated(Object rawMessage) {
        EventEnvelope<BloodRequestCreatedEvent> envelope = convertEnvelope(rawMessage, BloodRequestCreatedEvent.class);
        String eventId = envelope.getEventId();
        BloodRequestCreatedEvent event = envelope.getPayload();

        log.info("Received BloodRequestCreatedEvent [eventId={}, requestId={}, urgency={}]",
                eventId, event.getRequestId(), event.getUrgency());

        if (logRepository.existsByEventId(eventId)) {
            log.warn("Idempotency check triggered: Event [eventId={}] already processed. Skipping.", eventId);
            return;
        }

        String urgency = event.getUrgency() != null ? event.getUrgency().toUpperCase() : "ROUTINE";

        try {
            if ("URGENT".equals(urgency) || "EMERGENCY".equals(urgency)) {
                NotificationTemplate template = templateService.getTemplateByCode("URGENT_REQUEST_ALERT");
                Map<String, Object> vars = Map.of(
                        "requestId", event.getRequestId(),
                        "hospitalName", event.getHospitalName() != null ? event.getHospitalName() : "Hospital #" + event.getHospitalId(),
                        "bloodGroup", event.getBloodGroupCode() != null ? event.getBloodGroupCode() : "N/A",
                        "urgency", urgency,
                        "units", event.getUnitsRequested() != null ? event.getUnitsRequested() : 1
                );

                String body = templateService.render(template.getBodyTemplate(), vars);
                String subject = templateService.render(template.getSubject(), vars);

                emailSender.sendEmail("staff-oncall@bloodbank.com", subject, body);
                smsSender.sendSms("+15550199", "CRITICAL ALERT: " + subject);

                saveLog(eventId, "BloodRequestCreatedEvent", "STAFF", "ON_CALL",
                        "EMAIL_SMS", "URGENT_REQUEST_ALERT", "SENT", null);
            } else {
                log.info("ROUTINE BloodRequestCreatedEvent [requestId={}]: Logged without active push alert.", event.getRequestId());
                saveLog(eventId, "BloodRequestCreatedEvent", "STAFF", "SYSTEM",
                        "NONE", "ROUTINE_LOGGED", "SKIPPED", "Routine request logged without push notification");
            }
        } catch (Exception ex) {
            log.error("Failed to process BloodRequestCreatedEvent [eventId={}]", eventId, ex);
            saveLog(eventId, "BloodRequestCreatedEvent", "STAFF", "ON_CALL",
                    "EMAIL", "URGENT_REQUEST_ALERT", "FAILED", ex.getMessage());
            throw ex;
        }
    }

    @RabbitListener(
        queues = NotificationRabbitConfig.MASTER_LOOKUP_QUEUE,
        containerFactory = "lookupLogListenerContainerFactory"
    )
    @Transactional
    public void handleLookupItemChanged(Object rawMessage) {
        EventEnvelope<LookupItemChangedEvent> envelope = convertEnvelope(rawMessage, LookupItemChangedEvent.class);
        String eventId = envelope.getEventId();
        LookupItemChangedEvent event = envelope.getPayload();

        log.debug("Received LookupItemChangedEvent [eventId={}, category={}, item={}, changeType={}]",
                eventId, event.getCategoryCode(), event.getItemCode(), event.getChangeType());

        if (logRepository.existsByEventId(eventId)) {
            return;
        }

        saveLog(eventId, "LookupItemChangedEvent", "SYSTEM", "MASTER_SERVICE",
                "NONE", "LOOKUP_AUDIT", "SKIPPED", "Debug contract verification log");
    }

    private void saveLog(String eventId, String eventType, String recipientType, String recipientId,
                          String channel, String templateCode, String status, String failureReason) {
        try {
            NotificationLog nLog = new NotificationLog();
            nLog.setEventId(eventId);
            nLog.setEventType(eventType);
            nLog.setRecipientType(recipientType);
            nLog.setRecipientId(recipientId);
            nLog.setChannel(channel);
            nLog.setTemplateCode(templateCode);
            nLog.setStatus(status);
            nLog.setSentAt(Instant.now());
            nLog.setFailureReason(failureReason);
            nLog.setCreatedBy("NOTIFICATION_CONSUMER");
            nLog.setUpdatedBy("NOTIFICATION_CONSUMER");
            logRepository.save(nLog);
        } catch (DataIntegrityViolationException e) {
            log.warn("DB Unique Constraint Intercepted: NotificationLog for eventId={} already exists. Duplicate suppressed.", eventId);
        }
    }

    private <T extends DomainEvent> EventEnvelope<T> convertEnvelope(Object rawMessage, Class<T> payloadClass) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructParametricType(EventEnvelope.class, payloadClass);
            return objectMapper.convertValue(rawMessage, type);
        } catch (Exception e) {
            log.error("Failed to deserialize EventEnvelope for {}", payloadClass.getSimpleName(), e);
            throw new IllegalArgumentException("Invalid message payload", e);
        }
    }
}
