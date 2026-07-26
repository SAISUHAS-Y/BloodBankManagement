package com.bloodbank.common.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared DTO representing a Blood Bank facility summary, used across inventory and donor services.
 * 
 * DESIGN RULE: This DTO must NOT contain JPA annotations (like @Entity, @Table, @Column).
 * Adding JPA annotations would tightly couple the API contract to the Blood Bank Service's 
 * database schema, force downstream microservices to pull in JPA/Hibernate dependencies, 
 * and cause serialization issues (e.g. LazyInitializationExceptions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodBankSummaryResponse {
    
    private Long id;
    private String name;
    private String code;
    private String city;
    private String state;
    private String phoneNumber;
    private boolean active;
}
