package com.bloodbank.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Staff case note response payload")
public class DonorNoteResponse {

    private Long id;
    private Long donorId;
    private String noteText;
    private String authorUsername;
    private Instant createdAt;
}
