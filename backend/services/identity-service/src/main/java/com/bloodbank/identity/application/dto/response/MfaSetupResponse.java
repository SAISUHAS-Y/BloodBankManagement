package com.bloodbank.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload containing TOTP setup secret and authenticator QR URI")
public class MfaSetupResponse {

    @Schema(example = "JBSWY3DPEHPK3PXP", description = "Base32-encoded TOTP secret key for manual entry into authenticator apps")
    private String secretKey;

    @Schema(
        example = "otpauth://totp/BloodBank:john.doe@example.com?secret=JBSWY3DPEHPK3PXP&issuer=BloodBankManagement",
        description = "Standard otpauth:// URI for scanning with authenticator app QR code readers"
    )
    private String qrCodeUri;
}
