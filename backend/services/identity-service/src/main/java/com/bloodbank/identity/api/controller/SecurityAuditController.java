package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.core.dto.PageResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.identity.application.dto.AuthAuditLogQueryRequest;
import com.bloodbank.identity.application.dto.BlacklistIpRequest;
import com.bloodbank.identity.application.dto.SecurityAnalyticsSummaryResponse;
import com.bloodbank.identity.application.service.IpBlacklistService;
import com.bloodbank.identity.application.service.SecurityAnalyticsService;
import com.bloodbank.identity.domain.entity.AuthAuditLog;
import com.bloodbank.identity.domain.entity.BlacklistedIp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/security")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Security Audit & Threat Analytics", description = "Endpoints for investigating security logs, IP blacklisting, and analytics dashboards")
public class SecurityAuditController {

    private final SecurityAnalyticsService analyticsService;
    private final IpBlacklistService blacklistService;

    @PostMapping("/audit-logs/search")
    @HasPermission("AUDIT_LOG_VIEW")
    @Operation(summary = "Search Security Audit Logs", description = "Executes multi-criteria search over authentication audit logs")
    public ResponseEntity<ApiResponse<PageResponse<AuthAuditLog>>> searchAuditLogs(@RequestBody AuthAuditLogQueryRequest request) {
        PageResponse<AuthAuditLog> page = analyticsService.searchAuditLogs(request);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", page));
    }

    @GetMapping("/audit-logs/analytics")
    @HasPermission("AUDIT_LOG_VIEW")
    @Operation(summary = "Get Security Analytics Dashboard Summary", description = "Aggregates overall authentication metrics, failed login rates, and top failed origin IPs")
    public ResponseEntity<ApiResponse<SecurityAnalyticsSummaryResponse>> getAnalyticsSummary() {
        SecurityAnalyticsSummaryResponse summary = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/ip-blacklist")
    @HasPermission("SYSTEM_ADMIN")
    @Operation(summary = "Blacklist IP Address", description = "Adds a target IP address to the security blacklist to drop incoming traffic")
    public ResponseEntity<ApiResponse<BlacklistedIp>> blacklistIp(
            @Valid @RequestBody BlacklistIpRequest request,
            Principal principal) {
        String adminUser = principal != null ? principal.getName() : "ADMIN_PORTAL";
        BlacklistedIp blocked = blacklistService.blacklistIp(request, adminUser);
        return new ResponseEntity<>(ApiResponse.success("IP blacklisted successfully", blocked), HttpStatus.CREATED);
    }

    @GetMapping("/ip-blacklist")
    @HasPermission("AUDIT_LOG_VIEW")
    @Operation(summary = "Get All Blacklisted IPs", description = "Lists all currently blacklisted IP addresses")
    public ResponseEntity<ApiResponse<List<BlacklistedIp>>> getAllBlacklistedIps() {
        List<BlacklistedIp> blacklisted = blacklistService.getAllBlacklistedIps();
        return ResponseEntity.ok(ApiResponse.success(blacklisted));
    }

    @DeleteMapping("/ip-blacklist/{id}")
    @HasPermission("SYSTEM_ADMIN")
    @Operation(summary = "Remove IP from Blacklist", description = "Unblocks a blacklisted IP address by ID")
    public ResponseEntity<ApiResponse<Void>> removeIpFromBlacklist(@PathVariable Long id) {
        blacklistService.removeIpFromBlacklist(id);
        return ResponseEntity.ok(ApiResponse.success("IP unblocked successfully", null));
    }
}
