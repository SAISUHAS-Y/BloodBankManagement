package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.core.dto.PageResponse;
import com.bloodbank.identity.application.dto.AuthAuditLogQueryRequest;
import com.bloodbank.identity.application.dto.SecurityAnalyticsSummaryResponse;
import com.bloodbank.identity.application.service.SecurityAnalyticsService;
import com.bloodbank.identity.domain.entity.AuthAuditLog;
import com.bloodbank.identity.domain.enums.AuthEventType;
import com.bloodbank.identity.domain.repository.AuthAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAnalyticsServiceImpl implements SecurityAnalyticsService {

    private final AuthAuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthAuditLog> searchAuditLogs(AuthAuditLogQueryRequest request) {
        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy() != null ? request.getSortBy() : "occurredAt"
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<AuthAuditLog> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (request.getUserId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("userId"), request.getUserId()));
            }
            if (request.getEventType() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("eventType"), request.getEventType()));
            }
            if (request.getIpAddress() != null && !request.getIpAddress().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("ipAddress"), request.getIpAddress()));
            }
            if (request.getStartDate() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("occurredAt"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("occurredAt"), request.getEndDate()));
            }
            return predicates;
        };

        Page<AuthAuditLog> page = auditLogRepository.findAll(spec, pageable);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityAnalyticsSummaryResponse getAnalyticsSummary() {
        List<AuthAuditLog> logs = auditLogRepository.findAll();
        long totalEvents = logs.size();
        long successfulLogins = logs.stream().filter(l -> l.getEventType() == AuthEventType.LOGIN_SUCCESS).count();
        long failedLogins = logs.stream().filter(l -> l.getEventType() == AuthEventType.LOGIN_FAILED).count();

        double failedRate = totalEvents > 0 ? ((double) failedLogins / totalEvents) * 100.0 : 0.0;

        Map<String, Long> eventTypeDistribution = new HashMap<>();
        for (AuthAuditLog log : logs) {
            String eventKey = log.getEventType() != null ? log.getEventType().name() : "UNKNOWN";
            eventTypeDistribution.put(eventKey, eventTypeDistribution.getOrDefault(eventKey, 0L) + 1);
        }

        Map<String, Long> failedByIp = new HashMap<>();
        logs.stream()
                .filter(l -> l.getEventType() == AuthEventType.LOGIN_FAILED && l.getIpAddress() != null)
                .forEach(l -> failedByIp.put(l.getIpAddress(), failedByIp.getOrDefault(l.getIpAddress(), 0L) + 1));

        List<SecurityAnalyticsSummaryResponse.IpFailedCount> topFailedIps = failedByIp.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new SecurityAnalyticsSummaryResponse.IpFailedCount(e.getKey(), e.getValue()))
                .toList();

        return SecurityAnalyticsSummaryResponse.builder()
                .totalEvents(totalEvents)
                .successfulLogins(successfulLogins)
                .failedLogins(failedLogins)
                .failedLoginRatePercentage(Math.round(failedRate * 100.0) / 100.0)
                .eventTypeDistribution(eventTypeDistribution)
                .topFailedIps(topFailedIps)
                .build();
    }
}
