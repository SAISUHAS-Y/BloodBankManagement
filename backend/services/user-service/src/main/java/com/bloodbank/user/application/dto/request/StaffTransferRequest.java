package com.bloodbank.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for transferring staff between blood bank or hospital locations")
public class StaffTransferRequest {

    @Schema(example = "10", description = "Target Blood Bank ID")
    private Long bloodBankId;

    @Schema(example = "25", description = "Target Hospital ID")
    private Long hospitalId;

    @Schema(example = "Reassigned to central facility", description = "Transfer justification note")
    private String reason;
}
