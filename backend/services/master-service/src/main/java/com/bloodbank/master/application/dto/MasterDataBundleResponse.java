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
public class MasterDataBundleResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<BloodGroupResponse> bloodGroups;
    private List<LookupItemResponse> genders;
    private List<LookupItemResponse> documentTypes;
    private List<LookupItemResponse> titles;
    private List<LookupItemResponse> idTypes;
    private List<LookupItemResponse> donationTypes;
    private List<LookupItemResponse> deferralReasons;
    private List<LookupItemResponse> maritalStatuses;
    private List<LookupItemResponse> relationships;
    private List<LookupItemResponse> componentTypes;
}
