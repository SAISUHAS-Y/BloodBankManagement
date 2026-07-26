package com.bloodbank.user.application.service.impl;

import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.dto.BloodGroupContractResponse;
import com.bloodbank.common.contracts.dto.RecordDonationRequest;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.user.application.dto.DonorBasicResponse;
import com.bloodbank.user.application.dto.DonorProfileRequest;
import com.bloodbank.user.application.dto.DonorProfileResponse;
import com.bloodbank.user.application.service.DonorProfileService;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.entity.ProcessedDonation;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import com.bloodbank.user.domain.repository.ProcessedDonationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorProfileServiceImpl implements DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final ProcessedDonationRepository processedDonationRepository;
    private final MasterServiceClient masterServiceClient;
    private final MasterDataResolver dataResolver;

    @Override
    @Transactional
    public DonorProfileResponse createDonor(DonorProfileRequest request, String createdBy) {
        log.info("Creating donor profile for: {}", request.getFullName());
        validateLookupReferences(request);

        if (donorProfileRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new InvalidInputException("Donor already registered with phone number: " + request.getPhone());
        }

        DonorProfile donor = new DonorProfile();
        updateEntityFields(donor, request);
        donor.setCreatedBy(createdBy);
        donor.setUpdatedBy(createdBy);

        DonorProfile saved = donorProfileRepository.save(donor);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DonorProfileResponse updateDonor(Long id, DonorProfileRequest request, String updatedBy) {
        log.info("Updating donor profile ID: {}", id);
        DonorProfile donor = donorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found with ID: " + id));

        validateLookupReferences(request);

        var existing = donorProfileRepository.findByPhone(request.getPhone());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new InvalidInputException("Another donor already registered with phone number: " + request.getPhone());
        }

        updateEntityFields(donor, request);
        donor.setUpdatedBy(updatedBy);

        DonorProfile saved = donorProfileRepository.save(donor);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfileResponse getDonorById(Long id) {
        log.info("Fetching full sensitive donor profile ID: {}", id);
        DonorProfile donor = donorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found with ID: " + id));
        return mapToResponse(donor);
    }

    @Override
    @Transactional(readOnly = true)
    public DonorBasicResponse getDonorBasicById(Long id) {
        log.info("Fetching non-sensitive basic donor profile ID: {}", id);
        DonorProfile donor = donorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found with ID: " + id));
        return mapToBasicResponse(donor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonorProfileResponse> searchDonors(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size) {
        log.info("Searching full sensitive donors: bloodGroupId={} stateId={} districtId={} cityId={} status={}",
                bloodGroupId, stateId, districtId, cityId, status);

        DonorStatus donorStatus = parseStatus(status);
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<DonorProfile> donorPage = donorProfileRepository.searchDonors(bloodGroupId, stateId, districtId, cityId, donorStatus, pageable);

        return donorPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonorBasicResponse> searchDonorsBasic(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size) {
        log.info("Searching non-sensitive basic donors: bloodGroupId={} stateId={} districtId={} cityId={} status={}",
                bloodGroupId, stateId, districtId, cityId, status);

        DonorStatus donorStatus = parseStatus(status);
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<DonorProfile> donorPage = donorProfileRepository.searchDonors(bloodGroupId, stateId, districtId, cityId, donorStatus, pageable);

        return donorPage.map(this::mapToBasicResponse);
    }

    @Override
    @Transactional
    public void deferOrBlacklistDonor(Long id, String status, String reasonCode, String updatedBy) {
        log.info("Transitioning donor ID={} status to {} with reasonCode={}", id, status, reasonCode);

        DonorProfile donor = donorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found with ID: " + id));

        DonorStatus targetStatus = parseStatus(status);

        if (targetStatus == DonorStatus.ACTIVE) {
            throw new InvalidInputException("Transition to ACTIVE status must occur via standard checkout/clearing processes.");
        }

        Boolean reasonValid = masterServiceClient.validateLookupItemActive("DEFERRAL_REASON", reasonCode).getData();
        if (reasonValid == null || !reasonValid) {
            throw new InvalidInputException("Invalid deferral reason code: " + reasonCode);
        }

        donor.setDonorStatus(targetStatus);
        donor.setUpdatedBy(updatedBy);
        donorProfileRepository.save(donor);
    }

    @Override
    @Transactional
    public DonorProfileResponse recordDonation(Long id, RecordDonationRequest request) {
        log.info("Recording donation for donor ID: {}, donation ID: {}", id, request.getDonationId());

        DonorProfile donor = donorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found with ID: " + id));

        if (processedDonationRepository.existsById(request.getDonationId())
                || (donor.getLastProcessedDonationId() != null && donor.getLastProcessedDonationId().equals(request.getDonationId()))) {
            log.warn("Donation ID: {} already processed. Skipping duplicate record donation request.", request.getDonationId());
            return mapToResponse(donor);
        }

        try {
            donor.setLastDonationDate(request.getDonationDate());
            donor.setTotalDonations(donor.getTotalDonations() + 1);
            donor.setLastProcessedDonationId(request.getDonationId());
            DonorProfile saved = donorProfileRepository.save(donor);

            ProcessedDonation processed = new ProcessedDonation(request.getDonationId(), Instant.now());
            processedDonationRepository.save(processed);

            log.info("Successfully updated statistics for donor ID: {}", id);
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate donation ID: {} caught via DB unique constraint. Skipping duplicate execution.", request.getDonationId());
            return mapToResponse(donor);
        }
    }

    private DonorStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return DonorStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid donor status: " + status);
        }
    }

    private void validateLookupReferences(DonorProfileRequest request) {
        Boolean genderValid = masterServiceClient.validateLookupItemActive("GENDER", request.getGenderCode()).getData();
        if (genderValid == null || !genderValid) {
            throw new InvalidInputException("Invalid or inactive gender code: " + request.getGenderCode());
        }

        List<BloodGroupContractResponse> groups = masterServiceClient.getBloodGroups().getData();
        boolean bloodGroupIdValid = groups != null && groups.stream().anyMatch(g -> g.getId().equals(request.getBloodGroupId()));
        if (!bloodGroupIdValid) {
            throw new InvalidInputException("Invalid blood group ID: " + request.getBloodGroupId());
        }
    }

    private void updateEntityFields(DonorProfile donor, DonorProfileRequest request) {
        donor.setIdentityUserId(request.getIdentityUserId());
        donor.setFullName(request.getFullName());
        donor.setDob(request.getDob());
        donor.setGenderCode(request.getGenderCode());
        donor.setBloodGroupId(request.getBloodGroupId());
        donor.setPhone(request.getPhone());
        donor.setEmail(request.getEmail());
        donor.setAddressLine(request.getAddressLine());
        donor.setStateId(request.getStateId());
        donor.setDistrictId(request.getDistrictId());
        donor.setCityId(request.getCityId());
        donor.setIdType(request.getIdType());
        donor.setIdNumber(request.getIdNumber());
    }

    private DonorProfileResponse mapToResponse(DonorProfile donor) {
        return DonorProfileResponse.builder()
                .id(donor.getId())
                .identityUserId(donor.getIdentityUserId())
                .fullName(donor.getFullName())
                .dob(donor.getDob())
                .genderCode(donor.getGenderCode())
                .genderLabel(dataResolver.getGenderLabel(donor.getGenderCode()))
                .bloodGroupId(donor.getBloodGroupId())
                .bloodGroupLabel(dataResolver.getBloodGroupLabel(donor.getBloodGroupId()))
                .phone(donor.getPhone())
                .email(donor.getEmail())
                .addressLine(donor.getAddressLine())
                .stateId(donor.getStateId())
                .stateName(dataResolver.getStateName(donor.getStateId()))
                .districtId(donor.getDistrictId())
                .districtName(dataResolver.getDistrictName(donor.getStateId(), donor.getDistrictId()))
                .cityId(donor.getCityId())
                .cityName(dataResolver.getCityName(donor.getDistrictId(), donor.getCityId()))
                .idType(donor.getIdType())
                .idNumber(donor.getIdNumber())
                .donorStatus(donor.getDonorStatus().name())
                .lastDonationDate(donor.getLastDonationDate())
                .deferredUntilDate(donor.getDeferredUntilDate())
                .totalDonations(donor.getTotalDonations())
                .build();
    }

    private DonorBasicResponse mapToBasicResponse(DonorProfile donor) {
        return DonorBasicResponse.builder()
                .id(donor.getId())
                .fullName(donor.getFullName())
                .genderCode(donor.getGenderCode())
                .genderLabel(dataResolver.getGenderLabel(donor.getGenderCode()))
                .bloodGroupId(donor.getBloodGroupId())
                .bloodGroupLabel(dataResolver.getBloodGroupLabel(donor.getBloodGroupId()))
                .stateId(donor.getStateId())
                .stateName(dataResolver.getStateName(donor.getStateId()))
                .districtId(donor.getDistrictId())
                .districtName(dataResolver.getDistrictName(donor.getStateId(), donor.getDistrictId()))
                .cityId(donor.getCityId())
                .cityName(dataResolver.getCityName(donor.getDistrictId(), donor.getCityId()))
                .donorStatus(donor.getDonorStatus().name())
                .lastDonationDate(donor.getLastDonationDate())
                .totalDonations(donor.getTotalDonations())
                .build();
    }
}
