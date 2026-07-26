package com.bloodbank.common.events.user;

import com.bloodbank.common.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete domain event published when a new user is created in the identity service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements DomainEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant occurredAt = Instant.now();

    private Long userId;
    private String username;
    private String email;
    private String fullName;
}
