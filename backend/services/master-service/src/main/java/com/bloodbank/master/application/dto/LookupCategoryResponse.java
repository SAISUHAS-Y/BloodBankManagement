package com.bloodbank.master.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupCategoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean systemCategory;
    private List<LookupItemResponse> items;
}
