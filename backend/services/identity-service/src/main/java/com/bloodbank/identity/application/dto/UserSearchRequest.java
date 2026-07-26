package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "Criteria payload for advanced multi-field user search and pagination")
public class UserSearchRequest {

    @Schema(example = "smith", description = "Free-text search across username, email, and full name")
    private String searchTerm;

    @Schema(example = "ACTIVE", description = "Filter by account status (PENDING_VERIFICATION, ACTIVE, LOCKED, SUSPENDED, DEACTIVATED)")
    private String status;

    @Schema(example = "ROLE_LAB_TECHNICIAN", description = "Filter users possessing specific role code")
    private String roleCode;

    @Schema(example = "DEFAULT_TENANT", description = "Tenant isolation filter")
    private String tenantId;

    @Schema(description = "Filter users created on or after this timestamp")
    private Instant createdFrom;

    @Schema(description = "Filter users created on or before this timestamp")
    private Instant createdTo;

    @Min(value = 0, message = "Page index cannot be negative")
    @Schema(example = "0", description = "Zero-indexed page number")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(example = "20", description = "Page result limit")
    private int size = 20;

    @Schema(example = "createdAt", description = "Field name to sort results by")
    private String sortBy = "createdAt";

    @Schema(example = "DESC", description = "Sort direction: ASC or DESC")
    private String sortDirection = "DESC";
}
