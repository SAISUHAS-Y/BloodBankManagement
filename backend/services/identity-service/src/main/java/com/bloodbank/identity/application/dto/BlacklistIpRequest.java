package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request model to add an IP address to the security blacklist")
public class BlacklistIpRequest {

    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^[0-9a-fA-F:]+$", message = "Invalid IPv4 or IPv6 format")
    @Schema(example = "198.51.100.45", description = "Target IP address to blacklist")
    private String ipAddress;

    @Schema(example = "Suspicious brute-force credential stuffing activity detected", description = "Reason for blacklisting")
    private String reason;

    @Schema(example = "1440", description = "Blacklist duration in minutes (omit or pass null for permanent block)")
    private Integer durationMinutes;
}
