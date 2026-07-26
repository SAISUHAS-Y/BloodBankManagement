package com.bloodbank.common.events.transaction;

import com.bloodbank.common.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestCreatedEvent implements DomainEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant occurredAt = Instant.now();

    private Long requestId;
    private Long hospitalId;
    private String hospitalName;
    private String bloodGroupCode;
    private String componentTypeCode;
    private Double unitsRequested;
    private String urgency; // ROUTINE, URGENT, EMERGENCY
    private Instant requiredBy;
}
