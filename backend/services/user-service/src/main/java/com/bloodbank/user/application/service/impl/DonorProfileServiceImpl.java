package com.bloodbank.user.application.service.impl;

import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.dto.BloodGroupContractResponse;
import com.bloodbank.common.contracts.dto.RecordDonationRequest;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.user.application.dto.request.DonorNoteRequest;
import com.bloodbank.user.application.dto.request.DonorProfileRequest;
import com.bloodbank.user.application.dto.request.DonorSearchRequest;
import com.bloodbank.user.application.dto.response.DonorBasicResponse;
import com.bloodbank.user.application.dto.response.DonorBulkImportResultResponse;
import com.bloodbank.user.application.dto.response.DonorNoteResponse;
import com.bloodbank.user.application.dto.response.DonorProfileResponse;
import com.bloodbank.user.application.dto.response.ProfileAuditHistoryResponse;
import com.bloodbank.user.application.service.DonorProfileService;
import com.bloodbank.user.domain.entity.DonorNote;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.entity.ProcessedDonation;
import com.bloodbank.user.domain.entity.ProfileAuditHistory;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.repository.DonorNoteRepository;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import com.bloodbank.user.domain.repository.ProcessedDonationRepository;
import com.bloodbank.user.domain.repository.ProfileAuditHistoryRepository;
import com.bloodbank.user.domain.repository.specification.DonorSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorProfileServiceImpl implements DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final ProcessedDonationRepository processedDonationRepository;
    private final ProfileAuditHistoryRepository auditHistoryRepository;
    private final DonorNoteRepository donorNoteRepository;
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
        recordAuditHistory("DONOR", saved.getId(), "CREATE", null, saved.getDonorStatus().name(), "Initial profile creation", createdBy);
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

        String oldValues = String.format("name=%s, phone=%s, email=%s", donor.getFullName(), donor.getPhone(), donor.getEmail());
        updateEntityFields(donor, request);
        donor.setUpdatedBy(updatedBy);

        DonorProfile saved = donorProfileRepository.save(donor);
        String newValues = String.format("name=%s, phone=%s, email=%s", saved.getFullName(), saved.getPhone(), saved.getEmail());
        recordAuditHistory("DONOR", saved.getId(), "FIELD_UPDATE", oldValues, newValues, "Profile updated", updatedBy);

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
        DonorSearchRequest req = new DonorSearchRequest();
        req.setBloodGroupId(bloodGroupId);
        req.setStateId(stateId);
        req.setDistrictId(districtId);
        req.setCityId(cityId);
        req.setStatus(status);
        req.setPage(page);
        req.setSize(size);
        req.setSortBy("fullName");
        req.setSortDirection("ASC");
        return searchDonors(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonorBasicResponse> searchDonorsBasic(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size) {
        DonorSearchRequest req = new DonorSearchRequest();
        req.setBloodGroupId(bloodGroupId);
        req.setStateId(stateId);
        req.setDistrictId(districtId);
        req.setCityId(cityId);
        req.setStatus(status);
        req.setPage(page);
        req.setSize(size);
        req.setSortBy("fullName");
        req.setSortDirection("ASC");
        return searchDonorsBasic(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonorProfileResponse> searchDonors(DonorSearchRequest request) {
        log.info("Searching sensitive donors via specification: searchTerm={}, status={}", request.getSearchTerm(), request.getStatus());
        Pageable pageable = createPageable(request);
        var spec = DonorSpecifications.buildSearchSpecification(request);
        return donorProfileRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonorBasicResponse> searchDonorsBasic(DonorSearchRequest request) {
        log.info("Searching basic donors via specification: searchTerm={}, status={}", request.getSearchTerm(), request.getStatus());
        Pageable pageable = createPageable(request);
        var spec = DonorSpecifications.buildSearchSpecification(request);
        return donorProfileRepository.findAll(spec, pageable).map(this::mapToBasicResponse);
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

        String oldStatus = donor.getDonorStatus().name();
        donor.setDonorStatus(targetStatus);
        donor.setUpdatedBy(updatedBy);
        donorProfileRepository.save(donor);

        recordAuditHistory("DONOR", id, "STATUS_CHANGE", oldStatus, targetStatus.name(), "Reason code: " + reasonCode, updatedBy);
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

    @Override
    @Transactional(readOnly = true)
    public List<ProfileAuditHistoryResponse> getDonorHistory(Long donorId) {
        log.info("Fetching audit history for donor ID: {}", donorId);
        if (!donorProfileRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor profile not found with ID: " + donorId);
        }
        return auditHistoryRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("DONOR", donorId)
                .stream()
                .map(this::mapToAuditResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DonorNoteResponse addDonorNote(Long donorId, DonorNoteRequest request, String authorUsername) {
        log.info("Adding case note for donor ID: {} by user: {}", donorId, authorUsername);
        if (!donorProfileRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor profile not found with ID: " + donorId);
        }

        DonorNote note = new DonorNote();
        note.setDonorId(donorId);
        note.setNoteText(request.getNoteText());
        note.setAuthorUsername(authorUsername);
        note.setCreatedBy(authorUsername);
        note.setUpdatedBy(authorUsername);

        DonorNote saved = donorNoteRepository.save(note);
        return mapToNoteResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorNoteResponse> getDonorNotes(Long donorId) {
        log.info("Fetching case notes for donor ID: {}", donorId);
        if (!donorProfileRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor profile not found with ID: " + donorId);
        }
        return donorNoteRepository.findByDonorIdOrderByCreatedAtDesc(donorId)
                .stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DonorBulkImportResultResponse bulkImportDonors(MultipartFile file, String createdBy) {
        log.info("Executing bulk donor import from file: {}", file.getOriginalFilename());
        DonorBulkImportResultResponse result = new DonorBulkImportResultResponse();
        int rowNumber = 0;
        int successCount = 0;
        int failureCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip CSV header
                }

                if (!StringUtils.hasText(line)) continue;

                String[] tokens = line.split(",", -1);
                if (tokens.length < 10) {
                    failureCount++;
                    result.getErrors().add(new DonorBulkImportResultResponse.RowError(rowNumber, line, "Insufficient columns. Expected at least 10 fields."));
                    continue;
                }

                try {
                    DonorProfileRequest req = DonorProfileRequest.builder()
                            .fullName(tokens[0].trim())
                            .dob(LocalDate.parse(tokens[1].trim()))
                            .genderCode(tokens[2].trim())
                            .bloodGroupId(Long.parseLong(tokens[3].trim()))
                            .phone(tokens[4].trim())
                            .email(tokens.length > 5 && StringUtils.hasText(tokens[5]) ? tokens[5].trim() : null)
                            .addressLine(tokens.length > 6 && StringUtils.hasText(tokens[6]) ? tokens[6].trim() : null)
                            .stateId(Long.parseLong(tokens[7].trim()))
                            .districtId(Long.parseLong(tokens[8].trim()))
                            .cityId(Long.parseLong(tokens[9].trim()))
                            .idType(tokens.length > 10 && StringUtils.hasText(tokens[10]) ? tokens[10].trim() : null)
                            .idNumber(tokens.length > 11 && StringUtils.hasText(tokens[11]) ? tokens[11].trim() : null)
                            .build();

                    createDonor(req, createdBy);
                    successCount++;
                } catch (Exception ex) {
                    failureCount++;
                    result.getErrors().add(new DonorBulkImportResultResponse.RowError(rowNumber, line, ex.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to process CSV file upload", e);
            throw new InvalidInputException("Failed to read CSV import file: " + e.getMessage());
        }

        result.setTotalRowsProcessed(rowNumber > 0 ? rowNumber - 1 : 0);
        result.setSuccessfulCount(successCount);
        result.setFailedCount(failureCount);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportDonorsCsv(DonorSearchRequest searchRequest) {
        log.info("Generating CSV export for donors");
        searchRequest.setPage(0);
        searchRequest.setSize(1000); // Export up to 1000 matching records
        Page<DonorProfileResponse> page = searchDonors(searchRequest);

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Full Name,DOB,Gender Code,Blood Group ID,Phone,Email,Address,State ID,District ID,City ID,Status,Last Donation Date,Total Donations\n");

        for (DonorProfileResponse d : page.getContent()) {
            sb.append(d.getId()).append(",")
                    .append(escapeCsv(d.getFullName())).append(",")
                    .append(d.getDob()).append(",")
                    .append(escapeCsv(d.getGenderCode())).append(",")
                    .append(d.getBloodGroupId()).append(",")
                    .append(escapeCsv(d.getPhone())).append(",")
                    .append(escapeCsv(d.getEmail())).append(",")
                    .append(escapeCsv(d.getAddressLine())).append(",")
                    .append(d.getStateId()).append(",")
                    .append(d.getDistrictId()).append(",")
                    .append(d.getCityId()).append(",")
                    .append(d.getDonorStatus()).append(",")
                    .append(d.getLastDonationDate() != null ? d.getLastDonationDate() : "").append(",")
                    .append(d.getTotalDonations()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
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

    private DonorNoteResponse mapToNoteResponse(DonorNote note) {
        return DonorNoteResponse.builder()
                .id(note.getId())
                .donorId(note.getDonorId())
                .noteText(note.getNoteText())
                .authorUsername(note.getAuthorUsername())
                .createdAt(note.getCreatedAt())
                .build();
    }

    private Pageable createPageable(DonorSearchRequest request) {
        String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "createdAt";
        Sort sort = "ASC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(Math.max(0, request.getPage()), Math.max(1, request.getSize()), sort);
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
