package com.bloodbank.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a staff case note on a donor profile")
public class DonorNoteRequest {

    @NotBlank(message = "Note text cannot be empty")
    @Schema(example = "Donor reported feeling slightly dizzy after last donation. Cleared after 15 mins rest.", description = "Case note body text")
    private String noteText;
}
