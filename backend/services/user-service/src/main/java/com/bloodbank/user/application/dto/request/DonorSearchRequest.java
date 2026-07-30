package com.bloodbank.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Criteria payload for dynamic donor search and pagination")
public class DonorSearchRequest {

    @Schema(example = "John", description = "Free-text search across name, email, phone, or ID number")
    private String searchTerm;

    @Schema(example = "1", description = "Filter by blood group ID")
    private Long bloodGroupId;

    @Schema(example = "10", description = "Filter by state ID")
    private Long stateId;

    @Schema(example = "101", description = "Filter by district ID")
    private Long districtId;

    @Schema(example = "1001", description = "Filter by city ID")
    private Long cityId;

    @Schema(example = "ACTIVE", description = "Filter by donor status (ACTIVE, DEFERRED, BLACKLISTED)")
    private String status;

    @Schema(example = "MALE", description = "Filter by gender code")
    private String genderCode;

    @Schema(description = "Filter donors with last donation on or after this date")
    private LocalDate lastDonationFrom;

    @Schema(description = "Filter donors with last donation on or before this date")
    private LocalDate lastDonationTo;

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
