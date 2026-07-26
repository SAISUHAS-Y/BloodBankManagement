package com.bloodbank.notification.application.service;

import com.bloodbank.common.exception.DuplicateResourceException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.notification.application.dto.DlqSummaryResponse;
import com.bloodbank.notification.application.dto.NotificationLogResponse;
import com.bloodbank.notification.application.dto.NotificationTemplateDto;
import com.bloodbank.notification.application.dto.NotificationTemplateResponse;
import com.bloodbank.notification.application.dto.TemplatePreviewResponse;
import com.bloodbank.notification.domain.entity.NotificationLog;
import com.bloodbank.notification.domain.entity.NotificationTemplate;
import com.bloodbank.notification.domain.repository.NotificationLogRepository;
import com.bloodbank.notification.domain.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAdminService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationTemplateService templateService;
    private final RabbitAdmin rabbitAdmin;

    @Transactional
    public NotificationTemplateResponse createTemplate(NotificationTemplateDto dto) {
        if (templateRepository.existsByCodeAndIsDeletedFalse(dto.getCode())) {
            throw new DuplicateResourceException("NotificationTemplate code already exists: " + dto.getCode());
        }

        NotificationTemplate template = new NotificationTemplate();
        template.setCode(dto.getCode());
        template.setChannel(dto.getChannel().toUpperCase());
        template.setSubject(dto.getSubject());
        template.setBodyTemplate(dto.getBodyTemplate());
        template.setActive(dto.isActive());
        template.setCreatedBy("ADMIN");
        template.setUpdatedBy("ADMIN");

        NotificationTemplate saved = templateRepository.save(template);
        log.info("Created NotificationTemplate [code={}, id={}]", saved.getCode(), saved.getId());
        return mapToTemplateResponse(saved);
    }

    @Transactional
    public NotificationTemplateResponse updateTemplate(Long id, NotificationTemplateDto dto) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate not found with ID: " + id));

        template.setChannel(dto.getChannel().toUpperCase());
        template.setSubject(dto.getSubject());
        template.setBodyTemplate(dto.getBodyTemplate());
        template.setActive(dto.isActive());
        template.setUpdatedBy("ADMIN");

        NotificationTemplate saved = templateRepository.save(template);
        log.info("Updated NotificationTemplate [code={}, id={}]", saved.getCode(), saved.getId());
        return mapToTemplateResponse(saved);
    }

    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate not found with ID: " + id));
        return mapToTemplateResponse(template);
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .filter(t -> !t.isDeleted())
                .map(this::mapToTemplateResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TemplatePreviewResponse previewTemplate(String code, Map<String, Object> sampleTokens) {
        log.info("Generating dry-run preview for template code: {}", code);
        NotificationTemplate template = templateRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate not found with code: " + code));

        String renderedSubject = templateService.render(template.getSubject(), sampleTokens);
        String renderedBody = templateService.render(template.getBodyTemplate(), sampleTokens);

        return TemplatePreviewResponse.builder()
                .templateCode(code)
                .subject(renderedSubject)
                .body(renderedBody)
                .build();
    }

    public DlqSummaryResponse getDlqSummary() {
        log.info("Querying AMQP Dead Letter Queues message counts");
        Map<String, Integer> counts = new LinkedHashMap<>();
        int total = 0;

        String[] dlqs = new String[]{
            "notification.donor.registered.dlq",
            "notification.donation.completed.dlq",
            "notification.blood.request.dlq",
            "notification.master.lookup.dlq"
        };

        for (String dlq : dlqs) {
            try {
                var props = rabbitAdmin.getQueueProperties(dlq);
                int count = 0;
                if (props != null && props.containsKey("QUEUE_MESSAGE_COUNT")) {
                    count = ((Number) props.get("QUEUE_MESSAGE_COUNT")).intValue();
                }
                counts.put(dlq, count);
                total += count;
            } catch (Exception e) {
                log.warn("Could not retrieve message count for DLQ: {}", dlq, e);
                counts.put(dlq, 0);
            }
        }

        return DlqSummaryResponse.builder()
                .queueMessageCounts(counts)
                .totalDeadLetters(total)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<NotificationLogResponse> getLogs(
            String recipientId,
            String eventType,
            String status,
            Instant startDate,
            Instant endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationLog> logs = logRepository.findByFilters(recipientId, eventType, status, startDate, endDate, pageable);
        return logs.map(this::mapToLogResponse);
    }

    private NotificationTemplateResponse mapToTemplateResponse(NotificationTemplate t) {
        return NotificationTemplateResponse.builder()
                .id(t.getId())
                .code(t.getCode())
                .channel(t.getChannel())
                .subject(t.getSubject())
                .bodyTemplate(t.getBodyTemplate())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private NotificationLogResponse mapToLogResponse(NotificationLog l) {
        return NotificationLogResponse.builder()
                .id(l.getId())
                .eventId(l.getEventId())
                .eventType(l.getEventType())
                .recipientType(l.getRecipientType())
                .recipientId(l.getRecipientId())
                .channel(l.getChannel())
                .templateCode(l.getTemplateCode())
                .status(l.getStatus())
                .sentAt(l.getSentAt())
                .failureReason(l.getFailureReason())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
