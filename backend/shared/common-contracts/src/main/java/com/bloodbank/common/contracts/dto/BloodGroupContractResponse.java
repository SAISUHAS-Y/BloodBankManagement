package com.bloodbank.common.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodGroupContractResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String displayName;
    private boolean universalDonor;
    private boolean universalRecipient;
}
