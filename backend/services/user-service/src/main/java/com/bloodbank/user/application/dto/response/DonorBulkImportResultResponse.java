package com.bloodbank.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk import execution summary and row error report")
public class DonorBulkImportResultResponse {

    private int totalRowsProcessed;
    private int successfulCount;
    private int failedCount;

    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RowError {
        private int rowNumber;
        private String rawLine;
        private String errorMessage;
    }
}
