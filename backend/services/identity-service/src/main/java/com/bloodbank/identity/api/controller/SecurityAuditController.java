package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.core.dto.PageResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.identity.application.dto.request.AuthAuditLogQueryRequest;
import com.bloodbank.identity.application.dto.response.SecurityAnalyticsSummaryResponse;
import com.bloodbank.identity.application.service.SecurityAnalyticsService;
import com.bloodbank.identity.domain.entity.AuthAuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/security")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Security Audit Logs", description = "Simple endpoints for searching login security audit logs and viewing security activity statistics")
public class SecurityAuditController {

    private final SecurityAnalyticsService analyticsService;

    @PostMapping("/audit-logs/search")
    @HasPermission("AUDIT_LOG_VIEW")
    @Operation(summary = "Search Security Logs", description = "Search authentication logs by user ID, IP address, event type, or date")
    public ResponseEntity<ApiResponse<PageResponse<AuthAuditLog>>> searchAuditLogs(@RequestBody AuthAuditLogQueryRequest request) {
        PageResponse<AuthAuditLog> page = analyticsService.searchAuditLogs(request);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", page));
    }

    @GetMapping("/audit-logs/analytics")
    @HasPermission("AUDIT_LOG_VIEW")
    @Operation(summary = "View Security Dashboard Summary", description = "View login success rates, failed attempts, and top suspicious IPs")
    public ResponseEntity<ApiResponse<SecurityAnalyticsSummaryResponse>> getAnalyticsSummary() {
        SecurityAnalyticsSummaryResponse summary = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
