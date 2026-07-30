package com.bloodbank.bloodbank.application.service.impl;

import com.bloodbank.bloodbank.application.dto.request.BloodBankRequest;
import com.bloodbank.bloodbank.application.dto.response.BloodBankResponse;
import com.bloodbank.bloodbank.application.service.BloodBankService;
import com.bloodbank.bloodbank.domain.entity.BloodBank;
import com.bloodbank.bloodbank.domain.entity.BloodStock;
import com.bloodbank.bloodbank.domain.repository.BloodBankRepository;
import com.bloodbank.bloodbank.domain.repository.BloodStockRepository;
import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.dto.BloodGroupContractResponse;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodBankServiceImpl implements BloodBankService {

    private final BloodBankRepository bloodBankRepository;
    private final BloodStockRepository bloodStockRepository;
    private final MasterServiceClient masterServiceClient;
    private final MasterDataResolver dataResolver;

    @Override
    @Transactional
    public BloodBankResponse createBloodBank(BloodBankRequest request, String createdBy) {
        log.info("Creating blood bank facility: {}", request.getName());
        validateLookupReferences(request);

        if (bloodBankRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new InvalidInputException("Blood bank facility already registered with license number: " + request.getLicenseNumber());
        }

        BloodBank bb = new BloodBank();
        updateEntityFields(bb, request);
        bb.setCreatedBy(createdBy);
        bb.setUpdatedBy(createdBy);

        BloodBank saved = bloodBankRepository.save(bb);

        List<BloodGroupContractResponse> groups = masterServiceClient.getBloodGroups().getData();
        String[] componentTypes = {"RED_BLOOD_CELLS", "FRESH_FROZEN_PLASMA", "PLATELETS", "CRYOPRECIPITATE"};
        if (groups != null) {
            log.info("Pre-initializing zeroed-out stock rows for blood bank ID: {}", saved.getId());
            for (var group : groups) {
                for (var comp : componentTypes) {
                    BloodStock stock = new BloodStock();
                    stock.setBloodBank(saved);
                    stock.setBloodGroupId(group.getId());
                    stock.setComponentTypeCode(comp);
                    stock.setUnitsAvailable(0.0);
                    stock.setCreatedBy(createdBy);
                    stock.setUpdatedBy(createdBy);
                    bloodStockRepository.save(stock);
                }
            }
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BloodBankResponse updateBloodBank(Long id, BloodBankRequest request, String updatedBy) {
        log.info("Updating blood bank ID: {}", id);
        BloodBank bb = bloodBankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank facility not found with ID: " + id));

        validateLookupReferences(request);

        var existing = bloodBankRepository.findByLicenseNumber(request.getLicenseNumber());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new InvalidInputException("Another blood bank already registered with license number: " + request.getLicenseNumber());
        }

        updateEntityFields(bb, request);
        bb.setUpdatedBy(updatedBy);

        BloodBank saved = bloodBankRepository.save(bb);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BloodBankResponse getBloodBankById(Long id) {
        log.info("Fetching blood bank facility ID: {}", id);
        BloodBank bb = bloodBankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank facility not found with ID: " + id));
        return mapToResponse(bb);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BloodBankResponse> searchBloodBanks(Long stateId, Long districtId, Long cityId, Boolean isActive, int page, int size) {
        log.info("Searching blood banks: stateId={} districtId={} cityId={} isActive={}",
                stateId, districtId, cityId, isActive);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<BloodBank> bbPage = bloodBankRepository.searchBloodBanks(stateId, districtId, cityId, isActive, pageable);

        return bbPage.map(this::mapToResponse);
    }

    private void validateLookupReferences(BloodBankRequest request) {
        Boolean typeValid = masterServiceClient.validateLookupItemActive("BLOOD_BANK_TYPE", request.getBloodBankTypeCode()).getData();
        if (typeValid == null || !typeValid) {
            throw new InvalidInputException("Invalid or inactive blood bank type code: " + request.getBloodBankTypeCode());
        }
    }

    private void updateEntityFields(BloodBank bb, BloodBankRequest request) {
        bb.setName(request.getName());
        bb.setLicenseNumber(request.getLicenseNumber());
        bb.setBloodBankTypeCode(request.getBloodBankTypeCode());
        bb.setStateId(request.getStateId());
        bb.setDistrictId(request.getDistrictId());
        bb.setCityId(request.getCityId());
        bb.setAddressLine(request.getAddressLine());
        bb.setPhone(request.getPhone());
        bb.setEmail(request.getEmail());
        bb.setOperatingHoursNote(request.getOperatingHoursNote());
    }

    private BloodBankResponse mapToResponse(BloodBank bb) {
        return BloodBankResponse.builder()
                .id(bb.getId())
                .name(bb.getName())
                .licenseNumber(bb.getLicenseNumber())
                .bloodBankTypeCode(bb.getBloodBankTypeCode())
                .bloodBankTypeLabel(dataResolver.getBloodBankTypeLabel(bb.getBloodBankTypeCode()))
                .stateId(bb.getStateId())
                .stateName(dataResolver.getStateName(bb.getStateId()))
                .districtId(bb.getDistrictId())
                .districtName(dataResolver.getDistrictName(bb.getStateId(), bb.getDistrictId()))
                .cityId(bb.getCityId())
                .cityName(dataResolver.getCityName(bb.getDistrictId(), bb.getCityId()))
                .addressLine(bb.getAddressLine())
                .phone(bb.getPhone())
                .email(bb.getEmail())
                .operatingHoursNote(bb.getOperatingHoursNote())
                .active(bb.isActive())
                .build();
    }
}
