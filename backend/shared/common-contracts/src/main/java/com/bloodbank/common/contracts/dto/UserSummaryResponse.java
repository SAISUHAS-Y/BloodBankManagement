package com.bloodbank.common.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Shared DTO representing a simplified view of a User, typically used for Feign calls.
 * 
 * DESIGN RULE: This DTO must NOT contain JPA annotations (like @Entity, @Table, @Column).
 * Adding JPA annotations would tightly couple the API contract to the User Service's 
 * database schema, force downstream microservices to pull in JPA/Hibernate dependencies, 
 * and cause serialization issues (e.g. LazyInitializationExceptions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Set<String> permissions;
    private boolean active;
}
