package com.bloodbank.hospital.application.service;

import com.bloodbank.hospital.application.dto.HospitalRequest;
import com.bloodbank.hospital.application.dto.HospitalResponse;
import org.springframework.data.domain.Page;

public interface HospitalService {
    HospitalResponse createHospital(HospitalRequest request, String createdBy);
    HospitalResponse updateHospital(Long id, HospitalRequest request, String updatedBy);
    HospitalResponse getHospitalById(Long id);
    Page<HospitalResponse> searchHospitals(Long stateId, Long districtId, Long cityId, Boolean isActive, int page, int size);
    void deactivateOrDeleteHospital(Long id, String updatedBy);
}
