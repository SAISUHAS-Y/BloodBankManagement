package com.bloodbank.master.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLookupItemRequest {

    @NotBlank(message = "Label is required")
    private String label;

    @Min(value = 0, message = "Sort order must be 0 or positive")
    private int sortOrder;

    @NotNull(message = "Active state must be specified")
    private Boolean active;

    private String metadata;
}
