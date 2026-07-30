package com.bloodbank.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit trail entry for donor/staff status changes or field updates")
public class ProfileAuditHistoryResponse {

    private Long id;
    private String entityType;
    private Long entityId;
    private String actionType;
    private String oldValue;
    private String newValue;
    private String reason;
    private String changedBy;
    private Instant changedAt;
}
