package com.bloodbank.user.application.service.impl;

import com.bloodbank.common.contracts.client.IdentityServiceClient;
import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.user.application.dto.request.StaffProfileRequest;
import com.bloodbank.user.application.dto.request.StaffSearchRequest;
import com.bloodbank.user.application.dto.request.StaffStatusUpdateRequest;
import com.bloodbank.user.application.dto.request.StaffTransferRequest;
import com.bloodbank.user.application.dto.response.ProfileAuditHistoryResponse;
import com.bloodbank.user.application.dto.response.StaffHeadcountAnalyticsResponse;
import com.bloodbank.user.application.dto.response.StaffProfileResponse;
import com.bloodbank.user.application.service.StaffProfileService;
import com.bloodbank.user.domain.entity.ProfileAuditHistory;
import com.bloodbank.user.domain.entity.StaffProfile;
import com.bloodbank.user.domain.enums.StaffStatus;
import com.bloodbank.user.domain.repository.ProfileAuditHistoryRepository;
import com.bloodbank.user.domain.repository.StaffProfileRepository;
import com.bloodbank.user.domain.repository.specification.StaffSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffProfileServiceImpl implements StaffProfileService {

    private final StaffProfileRepository staffProfileRepository;
    private final ProfileAuditHistoryRepository auditHistoryRepository;
    private final IdentityServiceClient identityServiceClient;

    @Override
    @Transactional
    public StaffProfileResponse createStaff(StaffProfileRequest request, String createdBy) {
        log.info("Creating staff profile for: {}", request.getFullName());
        validateStaffExclusivity(request.getBloodBankId(), request.getHospitalId());
        validateIdentityUserExists(request.getIdentityUserId());
        validateReportingManager(request.getReportingManagerId());

        if (staffProfileRepository.findByIdentityUserId(request.getIdentityUserId()).isPresent()) {
            throw new InvalidInputException("A staff profile already exists for identity user ID: " + request.getIdentityUserId());
        }

        StaffProfile staff = new StaffProfile();
        updateEntityFields(staff, request);
        staff.setCreatedBy(createdBy);
        staff.setUpdatedBy(createdBy);

        StaffProfile saved = staffProfileRepository.save(staff);
        recordAuditHistory("STAFF", saved.getId(), "CREATE", null, saved.getStaffStatus().name(), "Initial staff profile creation", createdBy);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public StaffProfileResponse updateStaff(Long id, StaffProfileRequest request, String updatedBy) {
        log.info("Updating staff profile ID: {}", id);
        StaffProfile staff = staffProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with ID: " + id));

        validateStaffExclusivity(request.getBloodBankId(), request.getHospitalId());
        validateIdentityUserExists(request.getIdentityUserId());
        if (request.getReportingManagerId() != null && request.getReportingManagerId().equals(id)) {
            throw new InvalidInputException("Staff member cannot be their own reporting manager.");
        }
        validateReportingManager(request.getReportingManagerId());

        var existing = staffProfileRepository.findByIdentityUserId(request.getIdentityUserId());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new InvalidInputException("A staff profile already exists for identity user ID: " + request.getIdentityUserId());
        }

        String oldValues = String.format("name=%s, designation=%s, bb=%s, hosp=%s", staff.getFullName(), staff.getDesignation(), staff.getBloodBankId(), staff.getHospitalId());
        updateEntityFields(staff, request);
        staff.setUpdatedBy(updatedBy);

        StaffProfile saved = staffProfileRepository.save(staff);
        String newValues = String.format("name=%s, designation=%s, bb=%s, hosp=%s", saved.getFullName(), saved.getDesignation(), saved.getBloodBankId(), saved.getHospitalId());
        recordAuditHistory("STAFF", saved.getId(), "FIELD_UPDATE", oldValues, newValues, "Profile updated", updatedBy);

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

    @Override
    @Transactional
    public StaffProfileResponse updateStaffStatus(Long id, StaffStatusUpdateRequest request, String updatedBy) {
        log.info("Updating staff status ID: {} to {}", id, request.getStatus());
        StaffProfile staff = staffProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with ID: " + id));

        StaffStatus targetStatus;
        try {
            targetStatus = StaffStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid staff status: " + request.getStatus());
        }

        String oldStatus = staff.getStaffStatus().name();
        staff.setStaffStatus(targetStatus);
        staff.setUpdatedBy(updatedBy);

        StaffProfile saved = staffProfileRepository.save(staff);
        recordAuditHistory("STAFF", id, "STATUS_CHANGE", oldStatus, targetStatus.name(), request.getReason(), updatedBy);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public StaffProfileResponse transferStaff(Long id, StaffTransferRequest request, String updatedBy) {
        log.info("Transferring staff ID: {} bloodBankId={}, hospitalId={}", id, request.getBloodBankId(), request.getHospitalId());
        StaffProfile staff = staffProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with ID: " + id));

        validateStaffExclusivity(request.getBloodBankId(), request.getHospitalId());

        String oldLoc = String.format("bb=%s, hosp=%s", staff.getBloodBankId(), staff.getHospitalId());
        staff.setBloodBankId(request.getBloodBankId());
        staff.setHospitalId(request.getHospitalId());
        staff.setUpdatedBy(updatedBy);

        StaffProfile saved = staffProfileRepository.save(staff);
        String newLoc = String.format("bb=%s, hosp=%s", saved.getBloodBankId(), saved.getHospitalId());
        recordAuditHistory("STAFF", id, "TRANSFER", oldLoc, newLoc, request.getReason(), updatedBy);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffProfileResponse> searchStaff(StaffSearchRequest request) {
        log.info("Searching staff via specification: searchTerm={}, status={}, designation={}",
                request.getSearchTerm(), request.getStatus(), request.getDesignation());
        Pageable pageable = createPageable(request);
        var spec = StaffSpecifications.buildSearchSpecification(request);
        return staffProfileRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffProfileResponse> getDirectReports(Long staffId) {
        log.info("Fetching direct reports for manager staff ID: {}", staffId);
        if (!staffProfileRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Manager staff profile not found with ID: " + staffId);
        }
        return staffProfileRepository.findByReportingManagerId(staffId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileAuditHistoryResponse> getStaffHistory(Long staffId) {
        log.info("Fetching audit history for staff ID: {}", staffId);
        if (!staffProfileRepository.existsById(staffId)) {
            throw new ResourceNotFoundException("Staff profile not found with ID: " + staffId);
        }
        return auditHistoryRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("STAFF", staffId)
                .stream()
                .map(this::mapToAuditResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffHeadcountAnalyticsResponse getStaffHeadcountAnalytics() {
        log.info("Computing staff headcount analytics");
        List<StaffProfile> staffList = staffProfileRepository.findAll();

        long totalStaff = staffList.size();

        Map<String, Long> byStatus = staffList.stream()
                .collect(Collectors.groupingBy(s -> s.getStaffStatus().name(), Collectors.counting()));

        Map<String, Long> byDesignation = staffList.stream()
                .filter(s -> StringUtils.hasText(s.getDesignation()))
                .collect(Collectors.groupingBy(StaffProfile::getDesignation, Collectors.counting()));

        Map<Long, Long> byBloodBank = staffList.stream()
                .filter(s -> s.getBloodBankId() != null)
                .collect(Collectors.groupingBy(StaffProfile::getBloodBankId, Collectors.counting()));

        Map<Long, Long> byHospital = staffList.stream()
                .filter(s -> s.getHospitalId() != null)
                .collect(Collectors.groupingBy(StaffProfile::getHospitalId, Collectors.counting()));

        return StaffHeadcountAnalyticsResponse.builder()
                .totalStaff(totalStaff)
                .headcountByStatus(byStatus)
                .headcountByDesignation(byDesignation)
                .headcountByBloodBank(byBloodBank)
                .headcountByHospital(byHospital)
                .build();
    }

    private Pageable createPageable(StaffSearchRequest request) {
        String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "createdAt";
        Sort sort = "ASC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(Math.max(0, request.getPage()), Math.max(1, request.getSize()), sort);
    }

    private void validateStaffExclusivity(Long bloodBankId, Long hospitalId) {
        boolean hasBloodBank = bloodBankId != null;
        boolean hasHospital = hospitalId != null;

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

    private void validateReportingManager(Long managerId) {
        if (managerId != null && !staffProfileRepository.existsById(managerId)) {
            throw new InvalidInputException("Specified reporting manager staff profile does not exist with ID: " + managerId);
        }
    }

    private void recordAuditHistory(String entityType, Long entityId, String actionType, String oldValue, String newValue, String reason, String changedBy) {
        ProfileAuditHistory audit = new ProfileAuditHistory();
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setActionType(actionType);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setReason(reason);
        audit.setChangedBy(changedBy);
        audit.setCreatedBy(changedBy);
        audit.setUpdatedBy(changedBy);
        auditHistoryRepository.save(audit);
    }

    private ProfileAuditHistoryResponse mapToAuditResponse(ProfileAuditHistory audit) {
        return ProfileAuditHistoryResponse.builder()
                .id(audit.getId())
                .entityType(audit.getEntityType())
                .entityId(audit.getEntityId())
                .actionType(audit.getActionType())
                .oldValue(audit.getOldValue())
                .newValue(audit.getNewValue())
                .reason(audit.getReason())
                .changedBy(audit.getChangedBy())
                .changedAt(audit.getChangedAt())
                .build();
    }

    private void updateEntityFields(StaffProfile staff, StaffProfileRequest request) {
        staff.setIdentityUserId(request.getIdentityUserId());
        staff.setFullName(request.getFullName());
        staff.setDesignation(request.getDesignation());
        staff.setBloodBankId(request.getBloodBankId());
        staff.setHospitalId(request.getHospitalId());
        staff.setPhone(request.getPhone());
        staff.setReportingManagerId(request.getReportingManagerId());
    }

    private StaffProfileResponse mapToResponse(StaffProfile staff) {
        String managerName = null;
        if (staff.getReportingManagerId() != null) {
            managerName = staffProfileRepository.findById(staff.getReportingManagerId())
                    .map(StaffProfile::getFullName)
                    .orElse(null);
        }

        return StaffProfileResponse.builder()
                .id(staff.getId())
                .identityUserId(staff.getIdentityUserId())
                .fullName(staff.getFullName())
                .designation(staff.getDesignation())
                .bloodBankId(staff.getBloodBankId())
                .hospitalId(staff.getHospitalId())
                .phone(staff.getPhone())
                .staffStatus(staff.getStaffStatus().name())
                .reportingManagerId(staff.getReportingManagerId())
                .reportingManagerName(managerName)
                .build();
    }
}
