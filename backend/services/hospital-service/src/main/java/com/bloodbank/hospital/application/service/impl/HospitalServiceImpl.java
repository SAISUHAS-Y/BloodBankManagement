package com.bloodbank.hospital.application.service.impl;

import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.client.TransactionServiceClient;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.hospital.application.dto.HospitalContactResponse;
import com.bloodbank.hospital.application.dto.HospitalRequest;
import com.bloodbank.hospital.application.dto.HospitalResponse;
import com.bloodbank.hospital.application.service.HospitalService;
import com.bloodbank.hospital.domain.entity.Hospital;
import com.bloodbank.hospital.domain.entity.HospitalContact;
import com.bloodbank.hospital.domain.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final MasterServiceClient masterServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final MasterDataResolver dataResolver;

    @Override
    @Transactional
    public HospitalResponse createHospital(HospitalRequest request, String createdBy) {
        log.info("Creating hospital: {}", request.getName());
        validateLookupReferences(request);

        if (hospitalRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new InvalidInputException("Hospital already registered with registration number: " + request.getRegistrationNumber());
        }

        Hospital hospital = new Hospital();
        updateEntityFields(hospital, request);
        hospital.setCreatedBy(createdBy);
        hospital.setUpdatedBy(createdBy);

        if (request.getContacts() != null) {
            for (var cReq : request.getContacts()) {
                HospitalContact contact = new HospitalContact();
                contact.setHospital(hospital);
                contact.setName(cReq.getName());
                contact.setDesignation(cReq.getDesignation());
                contact.setPhone(cReq.getPhone());
                contact.setEmail(cReq.getEmail());
                contact.setCreatedBy(createdBy);
                contact.setUpdatedBy(createdBy);
                hospital.getContacts().add(contact);
            }
        }

        Hospital saved = hospitalRepository.save(hospital);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public HospitalResponse updateHospital(Long id, HospitalRequest request, String updatedBy) {
        log.info("Updating hospital ID: {}", id);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));

        validateLookupReferences(request);

        var existing = hospitalRepository.findByRegistrationNumber(request.getRegistrationNumber());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new InvalidInputException("Another hospital already registered with registration number: " + request.getRegistrationNumber());
        }

        updateEntityFields(hospital, request);
        hospital.setUpdatedBy(updatedBy);

        hospital.getContacts().clear();
        if (request.getContacts() != null) {
            for (var cReq : request.getContacts()) {
                HospitalContact contact = new HospitalContact();
                contact.setHospital(hospital);
                contact.setName(cReq.getName());
                contact.setDesignation(cReq.getDesignation());
                contact.setPhone(cReq.getPhone());
                contact.setEmail(cReq.getEmail());
                contact.setCreatedBy(updatedBy);
                contact.setUpdatedBy(updatedBy);
                hospital.getContacts().add(contact);
            }
        }

        Hospital saved = hospitalRepository.save(hospital);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponse getHospitalById(Long id) {
        log.info("Fetching hospital ID: {}", id);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));
        return mapToResponse(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HospitalResponse> searchHospitals(Long stateId, Long districtId, Long cityId, Boolean isActive, int page, int size) {
        log.info("Searching hospitals: stateId={} districtId={} cityId={} isActive={}",
                stateId, districtId, cityId, isActive);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Hospital> hospitalPage = hospitalRepository.searchHospitals(stateId, districtId, cityId, isActive, pageable);

        return hospitalPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deactivateOrDeleteHospital(Long id, String updatedBy) {
        log.info("Processing deletion/deactivation request for hospital ID: {}", id);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));

        boolean hasRequests = false;
        try {
            var response = transactionServiceClient.hasHospitalRequests(id);
            if (response != null && response.getData() != null) {
                hasRequests = response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to query transaction service for hospital blood requests. Defaulting to safe deactivation.", e);
            hasRequests = true; // Safety fallback
        }

        if (hasRequests) {
            log.info("Hospital ID {} has associated blood requests. Switching to DEACTIVATION-ONLY model.", id);
            hospital.setActive(false);
            hospital.setUpdatedBy(updatedBy);
            hospitalRepository.save(hospital);
        } else {
            log.info("Hospital ID {} has no associated blood requests. Performing soft delete.", id);
            hospital.delete();
            hospitalRepository.save(hospital);
        }
    }

    private void validateLookupReferences(HospitalRequest request) {
        Boolean typeValid = masterServiceClient.validateLookupItemActive("HOSPITAL_TYPE", request.getHospitalTypeCode()).getData();
        if (typeValid == null || !typeValid) {
            throw new InvalidInputException("Invalid or inactive hospital type code: " + request.getHospitalTypeCode());
        }
    }

    private void updateEntityFields(Hospital hospital, HospitalRequest request) {
        hospital.setName(request.getName());
        hospital.setRegistrationNumber(request.getRegistrationNumber());
        hospital.setHospitalTypeCode(request.getHospitalTypeCode());
        hospital.setStateId(request.getStateId());
        hospital.setDistrictId(request.getDistrictId());
        hospital.setCityId(request.getCityId());
        hospital.setAddressLine(request.getAddressLine());
        hospital.setPhone(request.getPhone());
        hospital.setEmail(request.getEmail());
    }

    private HospitalResponse mapToResponse(Hospital hospital) {
        List<HospitalContactResponse> contactDTOs = new ArrayList<>();
        if (hospital.getContacts() != null) {
            contactDTOs = hospital.getContacts().stream()
                    .map(c -> HospitalContactResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .designation(c.getDesignation())
                            .phone(c.getPhone())
                            .email(c.getEmail())
                            .build())
                    .collect(Collectors.toList());
        }

        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .registrationNumber(hospital.getRegistrationNumber())
                .hospitalTypeCode(hospital.getHospitalTypeCode())
                .hospitalTypeLabel(dataResolver.getHospitalTypeLabel(hospital.getHospitalTypeCode()))
                .stateId(hospital.getStateId())
                .stateName(dataResolver.getStateName(hospital.getStateId()))
                .districtId(hospital.getDistrictId())
                .districtName(dataResolver.getDistrictName(hospital.getStateId(), hospital.getDistrictId()))
                .cityId(hospital.getCityId())
                .cityName(dataResolver.getCityName(hospital.getDistrictId(), hospital.getCityId()))
                .addressLine(hospital.getAddressLine())
                .phone(hospital.getPhone())
                .email(hospital.getEmail())
                .active(hospital.isActive())
                .contacts(contactDTOs)
                .build();
    }
}
