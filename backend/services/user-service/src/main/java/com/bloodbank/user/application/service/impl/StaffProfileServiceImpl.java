package com.bloodbank.user.application.service.impl;

import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.contracts.client.IdentityServiceClient;
import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.user.application.dto.StaffProfileRequest;
import com.bloodbank.user.application.dto.StaffProfileResponse;
import com.bloodbank.user.application.service.StaffProfileService;
import com.bloodbank.user.domain.entity.StaffProfile;
import com.bloodbank.user.domain.repository.StaffProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffProfileServiceImpl implements StaffProfileService {

    private final StaffProfileRepository staffProfileRepository;
    private final IdentityServiceClient identityServiceClient;

    @Override
    @Transactional
    public StaffProfileResponse createStaff(StaffProfileRequest request, String createdBy) {
        log.info("Creating staff profile for: {}", request.getFullName());
        validateStaffExclusivity(request);
        validateIdentityUserExists(request.getIdentityUserId());

        if (staffProfileRepository.findByIdentityUserId(request.getIdentityUserId()).isPresent()) {
            throw new InvalidInputException("A staff profile already exists for identity user ID: " + request.getIdentityUserId());
        }

        StaffProfile staff = new StaffProfile();
        updateEntityFields(staff, request);
        staff.setCreatedBy(createdBy);
        staff.setUpdatedBy(createdBy);

        StaffProfile saved = staffProfileRepository.save(staff);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public StaffProfileResponse updateStaff(Long id, StaffProfileRequest request, String updatedBy) {
        log.info("Updating staff profile ID: {}", id);
        StaffProfile staff = staffProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with ID: " + id));

        validateStaffExclusivity(request);
        validateIdentityUserExists(request.getIdentityUserId());

        var existing = staffProfileRepository.findByIdentityUserId(request.getIdentityUserId());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new InvalidInputException("A staff profile already exists for identity user ID: " + request.getIdentityUserId());
        }

        updateEntityFields(staff, request);
        staff.setUpdatedBy(updatedBy);

        StaffProfile saved = staffProfileRepository.save(staff);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponse getStaffById(Long id) {
        log.info("Fetching staff profile ID: {}", id);
        StaffProfile staff = staffProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with ID: " + id));
        return mapToResponse(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponse getStaffByIdentityUserId(Long identityUserId) {
        log.info("Fetching staff profile for identity user ID: {}", identityUserId);
        StaffProfile staff = staffProfileRepository.findByIdentityUserId(identityUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found for identity user ID: " + identityUserId));
        return mapToResponse(staff);
    }

    private void validateStaffExclusivity(StaffProfileRequest request) {
        boolean hasBloodBank = request.getBloodBankId() != null;
        boolean hasHospital = request.getHospitalId() != null;

        if (hasBloodBank && hasHospital) {
            throw new InvalidInputException("A staff member cannot belong to both a blood bank and a hospital. Select exactly one.");
        }
        if (!hasBloodBank && !hasHospital) {
            throw new InvalidInputException("A staff member must be assigned to either a blood bank or a hospital.");
        }
    }

    private void validateIdentityUserExists(Long identityUserId) {
        try {
            UserSummaryResponse userSummary = identityServiceClient.getUserSummary(identityUserId).getData();
            if (userSummary == null || userSummary.getId() == null || "unknown_user".equals(userSummary.getUsername())) {
                throw new InvalidInputException("Linked identity user account does not exist or is unavailable.");
            }
        } catch (Exception e) {
            log.error("Identity user validation failed for ID: {}", identityUserId, e);
            throw new InvalidInputException("Could not validate identity user existence: " + e.getMessage());
        }
    }

    private void updateEntityFields(StaffProfile staff, StaffProfileRequest request) {
        staff.setIdentityUserId(request.getIdentityUserId());
        staff.setFullName(request.getFullName());
        staff.setDesignation(request.getDesignation());
        staff.setBloodBankId(request.getBloodBankId());
        staff.setHospitalId(request.getHospitalId());
        staff.setPhone(request.getPhone());
    }

    private StaffProfileResponse mapToResponse(StaffProfile staff) {
        return StaffProfileResponse.builder()
                .id(staff.getId())
                .identityUserId(staff.getIdentityUserId())
                .fullName(staff.getFullName())
                .designation(staff.getDesignation())
                .bloodBankId(staff.getBloodBankId())
                .hospitalId(staff.getHospitalId())
                .phone(staff.getPhone())
                .build();
    }
}
