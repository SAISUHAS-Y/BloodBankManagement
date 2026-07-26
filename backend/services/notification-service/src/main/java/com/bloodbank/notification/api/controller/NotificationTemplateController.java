package com.bloodbank.notification.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.notification.application.dto.NotificationTemplateDto;
import com.bloodbank.notification.application.dto.NotificationTemplateResponse;
import com.bloodbank.notification.application.dto.TemplatePreviewResponse;
import com.bloodbank.notification.application.service.NotificationAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationAdminService adminService;

    @PostMapping
    @HasPermission("NOTIFICATION_TEMPLATE_MANAGE")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> createTemplate(
            @Valid @RequestBody NotificationTemplateDto dto) {
        NotificationTemplateResponse response = adminService.createTemplate(dto);
        return new ResponseEntity<>(
                ApiResponse.success("Notification template created successfully", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @HasPermission("NOTIFICATION_TEMPLATE_MANAGE")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateDto dto) {
        NotificationTemplateResponse response = adminService.updateTemplate(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Notification template updated successfully", response));
    }

    @GetMapping("/{id}")
    @HasPermission("NOTIFICATION_TEMPLATE_MANAGE")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getTemplateById(@PathVariable Long id) {
        NotificationTemplateResponse response = adminService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success("Template retrieved successfully", response));
    }

    @GetMapping
    @HasPermission("NOTIFICATION_TEMPLATE_MANAGE")
    public ResponseEntity<ApiResponse<List<NotificationTemplateResponse>>> getAllTemplates() {
        List<NotificationTemplateResponse> response = adminService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success("All templates retrieved successfully", response));
    }

    @PostMapping("/{code}/preview")
    @HasPermission("NOTIFICATION_TEMPLATE_MANAGE")
    public ResponseEntity<ApiResponse<TemplatePreviewResponse>> previewTemplate(
            @PathVariable String code,
            @RequestBody Map<String, Object> sampleTokens) {
        
        TemplatePreviewResponse response = adminService.previewTemplate(code, sampleTokens);
        return ResponseEntity.ok(ApiResponse.success("Template dry-run rendered successfully", response));
    }
}
