package com.bloodbank.common.contracts.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Shared DTO representing a request to check donor eligibility before donation scheduling.
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
public class DonorEligibilityRequest {

    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @NotNull(message = "Last donation date is required")
    @Past(message = "Last donation date must be in the past")
    private LocalDate lastDonationDate;

    @NotNull(message = "Blood type is required")
    private String bloodType;

    private Integer age;
    private Double weightKg;
}
