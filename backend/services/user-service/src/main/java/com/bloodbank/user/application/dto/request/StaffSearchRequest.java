package com.bloodbank.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Criteria payload for dynamic staff search and pagination")
public class StaffSearchRequest {

    @Schema(example = "Alice", description = "Free-text search across full name, designation, or phone")
    private String searchTerm;

    @Schema(example = "LAB_TECHNICIAN", description = "Filter by staff designation")
    private String designation;

    @Schema(example = "5", description = "Filter by blood bank ID")
    private Long bloodBankId;

    @Schema(example = "12", description = "Filter by hospital ID")
    private Long hospitalId;

    @Schema(example = "ACTIVE", description = "Filter by staff status (ACTIVE, ON_LEAVE, SUSPENDED, TERMINATED)")
    private String status;

    @Schema(example = "2", description = "Filter by reporting manager staff ID")
    private Long reportingManagerId;

    @Min(value = 0, message = "Page index cannot be negative")
    @Schema(example = "0", description = "Zero-indexed page number")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(example = "10", description = "Page result limit")
    private int size = 10;

    @Schema(example = "createdAt", description = "Field name to sort results by")
    private String sortBy = "createdAt";

    @Schema(example = "DESC", description = "Sort direction: ASC or DESC")
    private String sortDirection = "DESC";
}
