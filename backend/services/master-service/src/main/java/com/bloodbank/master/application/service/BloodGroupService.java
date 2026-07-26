package com.bloodbank.master.application.service;

import com.bloodbank.master.application.dto.BloodGroupResponse;

import java.util.List;

public interface BloodGroupService {
    List<BloodGroupResponse> getAllBloodGroups();
    BloodGroupResponse getBloodGroupByCode(String code);
}
