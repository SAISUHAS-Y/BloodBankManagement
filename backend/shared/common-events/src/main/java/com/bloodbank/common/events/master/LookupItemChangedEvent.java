package com.bloodbank.common.events.master;

import com.bloodbank.common.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupItemChangedEvent implements DomainEvent {

    private String eventId;
    private Instant occurredAt;
    
    private String categoryCode;
    private String itemCode;
    private String changeType; // CREATED, UPDATED, DEACTIVATED
}
