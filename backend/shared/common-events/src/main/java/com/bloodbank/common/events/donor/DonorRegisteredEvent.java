package com.bloodbank.common.events.donor;

import com.bloodbank.common.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete domain event published when a new donor registers in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorRegisteredEvent implements DomainEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant occurredAt = Instant.now();

    private Long donorId;
    private String firstName;
    private String lastName;
    private String email;
    private String bloodType;
}
