package com.bloodbank.master.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLookupItemRequest {

    @NotBlank(message = "Category code is required")
    private String categoryCode;

    @NotBlank(message = "Item code is required")
    private String code;

    @NotBlank(message = "Label is required")
    private String label;

    @Min(value = 0, message = "Sort order must be 0 or positive")
    private int sortOrder;

    private String metadata;
}
