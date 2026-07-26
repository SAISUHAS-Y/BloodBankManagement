package com.bloodbank.master.application.service;

import com.bloodbank.master.application.dto.CityResponse;
import com.bloodbank.master.application.dto.DistrictResponse;
import com.bloodbank.master.application.dto.StateResponse;

import java.util.List;

public interface LocationService {
    List<StateResponse> getAllStates(boolean includeInactive);
    List<DistrictResponse> getDistrictsByState(Long stateId, boolean includeInactive);
    List<CityResponse> getCitiesByDistrict(Long districtId, boolean includeInactive);
    void deactivateState(Long stateId, String updatedBy);
}
