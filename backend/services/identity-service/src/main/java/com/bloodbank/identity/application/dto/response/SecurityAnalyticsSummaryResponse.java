package com.bloodbank.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated Security Audit Analytics Summary Payload")
public class SecurityAnalyticsSummaryResponse {

    @Schema(example = "12450", description = "Total authentication events recorded")
    private long totalEvents;

    @Schema(example = "10240", description = "Total successful login events")
    private long successfulLogins;

    @Schema(example = "410", description = "Total failed login attempts")
    private long failedLogins;

    @Schema(example = "3.88", description = "Failed login rate percentage")
    private double failedLoginRatePercentage;

    @Schema(description = "Distribution of security events grouped by event type")
    private Map<String, Long> eventTypeDistribution;

    @Schema(description = "Top 5 origin IP addresses with highest recorded failed login attempts")
    private List<IpFailedCount> topFailedIps;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IpFailedCount {
        private String ipAddress;
        private long failedCount;
    }
}
