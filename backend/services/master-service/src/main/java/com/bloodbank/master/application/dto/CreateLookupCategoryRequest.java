package com.bloodbank.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLookupCategoryRequest {

    @NotBlank(message = "Category code is required")
    @Size(max = 100, message = "Category code must not exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Category code must contain only uppercase letters, numbers, and underscores")
    private String code;

    @NotBlank(message = "Category name is required")
    @Size(max = 150, message = "Category name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
