package com.bloodbank.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Session Metadata Response Model")
public class SessionDetailsResponse {

    @Schema(example = "sess_99812a10-4b92-4112", description = "Session identifier")
    private String sessionId;

    @Schema(example = "df8192a2-3b12-4f8a-9211-e1293a109822", description = "Client device UUID")
    private String deviceId;

    @Schema(example = "Chrome 125.0 (Windows 11)", description = "Friendly device name")
    private String deviceName;

    @Schema(example = "Chrome 125.0 (Windows 11)", description = "User-Agent client software string")
    private String userAgent;

    @Schema(example = "192.168.1.105", description = "Origin client IP address")
    private String ipAddress;

    @Schema(example = "true", description = "Indicates whether this item represents the active session processing this request")
    private boolean isCurrentSession;

    @Schema(description = "Session creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last recorded activity timestamp")
    private Instant lastActivityAt;

    @Schema(description = "Hard session expiration timestamp")
    private Instant expiresAt;
}
