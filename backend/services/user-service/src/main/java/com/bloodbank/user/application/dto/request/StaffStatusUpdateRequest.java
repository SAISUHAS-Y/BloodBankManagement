package com.bloodbank.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating staff employment status")
public class StaffStatusUpdateRequest {

    @NotBlank(message = "Staff status is required")
    @Schema(example = "ON_LEAVE", description = "Target status (ACTIVE, ON_LEAVE, SUSPENDED, TERMINATED)")
    private String status;

    @Schema(example = "Maternity leave", description = "Reason for status change")
    private String reason;
}
