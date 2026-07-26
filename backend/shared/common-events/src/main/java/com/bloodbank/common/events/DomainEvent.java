package com.bloodbank.common.events;

import java.time.Instant;

/**
 * Marker interface for all domain events generated within the system.
 */
public interface DomainEvent {
    
    /**
     * @return Unique identifier of the event.
     */
    String getEventId();

    /**
     * @return The timestamp of when the event occurred.
     */
    Instant getOccurredAt();
}
