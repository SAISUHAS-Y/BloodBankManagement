package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload containing single-use emergency backup recovery codes")
public class MfaBackupCodesResponse {

    @Schema(
        example = "[\"A8K39F12X9\", \"P9201LKS92\", \"MN293019AA\"]",
        description = "List of 8 plain-text single-use 10-character emergency backup recovery codes. Store in a safe location!"
    )
    private List<String> backupCodes;
}
