package com.bloodbank.identity.application.dto;

import com.bloodbank.identity.domain.enums.AuthEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "Criteria query request for filtering security audit logs")
public class AuthAuditLogQueryRequest {

    @Schema(example = "1002", description = "Filter by specific user ID")
    private Long userId;

    @Schema(example = "LOGIN_FAILED", description = "Filter by security audit event type")
    private AuthEventType eventType;

    @Schema(example = "192.168.1.105", description = "Filter by origin IP address")
    private String ipAddress;

    @Schema(description = "Start time window filter")
    private Instant startDate;

    @Schema(description = "End time window filter")
    private Instant endDate;

    @Schema(example = "0", description = "Page number (0-indexed)")
    private int page = 0;

    @Schema(example = "20", description = "Page size")
    private int size = 20;

    @Schema(example = "occurredAt", description = "Property field to sort by")
    private String sortBy = "occurredAt";

    @Schema(example = "DESC", description = "Sort direction: ASC or DESC")
    private String sortDirection = "DESC";
}
