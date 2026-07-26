package com.bloodbank.common.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Shared DTO representing the response of a donor eligibility evaluation.
 * 
 * DESIGN RULE: This DTO must NOT contain JPA annotations (like @Entity, @Table, @Column).
 * Adding JPA annotations would tightly couple the API contract to the Donor Service's 
 * database schema, force downstream microservices to pull in JPA/Hibernate dependencies, 
 * and cause serialization issues (e.g. LazyInitializationExceptions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorEligibilityResponse {

    private Long donorId;
    private boolean eligible;
    private String ineligibilityReason;
    private LocalDate nextAvailableDate;
}
