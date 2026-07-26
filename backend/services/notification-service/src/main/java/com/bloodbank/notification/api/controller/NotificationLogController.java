package com.bloodbank.notification.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.notification.application.dto.DlqSummaryResponse;
import com.bloodbank.notification.application.dto.NotificationLogResponse;
import com.bloodbank.notification.application.service.NotificationAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationLogController {

    private final NotificationAdminService adminService;

    @GetMapping("/logs")
    @HasPermission("NOTIFICATION_VIEW")
    public ResponseEntity<ApiResponse<Page<NotificationLogResponse>>> getLogs(
            @RequestParam(required = false) String recipientId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationLogResponse> response = adminService.getLogs(recipientId, eventType, status, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Notification audit logs retrieved successfully", response));
    }

    @GetMapping("/admin/dlq-summary")
    @HasPermission("NOTIFICATION_VIEW")
    public ResponseEntity<ApiResponse<DlqSummaryResponse>> getDlqSummary() {
        DlqSummaryResponse response = adminService.getDlqSummary();
        return ResponseEntity.ok(ApiResponse.success("Dead Letter Queue summary retrieved successfully", response));
    }
}
