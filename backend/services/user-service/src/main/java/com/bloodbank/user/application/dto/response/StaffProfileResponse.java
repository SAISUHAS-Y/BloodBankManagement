package com.bloodbank.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long identityUserId;
    private String fullName;
    private String designation;
    private Long bloodBankId;
    private String bloodBankName;
    private Long hospitalId;
    private String hospitalName;
    private String phone;
    private String staffStatus;
    private Long reportingManagerId;
    private String reportingManagerName;
}
