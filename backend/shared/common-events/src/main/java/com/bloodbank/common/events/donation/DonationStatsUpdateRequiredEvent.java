package com.bloodbank.common.events.donation;

import com.bloodbank.common.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationStatsUpdateRequiredEvent implements DomainEvent, Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private Instant occurredAt;
    private Long donationId;
    private Long donorProfileId;
    private LocalDate donationDate;
    private double unitsCollected;
}
