package com.bloodbank.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Payload to complete password reset using a single-use verification token")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    @Schema(example = "rst_9912a10-4b92-4112", description = "Single-use password reset verification token")
    private String token;

    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
        message = "New password must be at least 8 characters long, containing uppercase, lowercase, digit, and special character"
    )
    @Schema(example = "NewSecureP@ss2026!", description = "New password compliant with complexity rules")
    private String newPassword;
}
