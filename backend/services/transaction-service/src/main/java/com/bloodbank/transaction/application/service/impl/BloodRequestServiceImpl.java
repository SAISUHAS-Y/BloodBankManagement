package com.bloodbank.transaction.application.service.impl;

import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.contracts.client.BloodBankServiceClient;
import com.bloodbank.common.contracts.client.HospitalServiceClient;
import com.bloodbank.common.contracts.dto.BloodStockAdjustRequest;
import com.bloodbank.common.contracts.dto.HospitalContractResponse;
import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.transaction.application.dto.BloodRequestDto;
import com.bloodbank.transaction.application.dto.BloodRequestResponse;
import com.bloodbank.transaction.application.dto.IssuanceRequest;
import com.bloodbank.transaction.application.dto.IssuanceResponse;
import com.bloodbank.transaction.application.event.TransactionEventPublisher;
import com.bloodbank.transaction.application.service.BloodRequestService;
import com.bloodbank.transaction.domain.entity.BloodRequest;
import com.bloodbank.transaction.domain.entity.IssuanceRecord;
import com.bloodbank.transaction.domain.enums.RequestStatus;
import com.bloodbank.transaction.domain.enums.Urgency;
import com.bloodbank.transaction.domain.repository.BloodRequestRepository;
import com.bloodbank.transaction.domain.repository.IssuanceRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodRequestServiceImpl implements BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final IssuanceRecordRepository issuanceRecordRepository;
    private final BloodBankServiceClient bloodBankServiceClient;
    private final HospitalServiceClient hospitalServiceClient;
    private final MasterDataResolver dataResolver;
    private final TransactionEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public BloodRequestResponse createRequest(BloodRequestDto requestDto, String username) {
        log.info("Creating blood request for hospital ID: {}", requestDto.getHospitalId());

        HospitalContractResponse hospital = hospitalServiceClient.getHospitalById(requestDto.getHospitalId()).getData();
        if (hospital == null || hospital.getId() == null) {
            throw new InvalidInputException("Linked hospital does not exist: " + requestDto.getHospitalId());
        }

        if (!hospital.isActive()) {
            throw new InvalidInputException("Linked hospital is currently inactive.");
        }

        BloodRequest req = new BloodRequest();
        req.setHospitalId(requestDto.getHospitalId());
        req.setRequestedBy(requestDto.getRequestedBy());
        req.setBloodGroupId(requestDto.getBloodGroupId());
        req.setComponentTypeCode(requestDto.getComponentTypeCode());
        req.setUnitsRequested(requestDto.getUnitsRequested());
        req.setPatientName(requestDto.getPatientName());
        req.setPatientAge(requestDto.getPatientAge());
        req.setClinicalReason(requestDto.getClinicalReason());
        req.setRequestedAt(Instant.now());
        req.setCreatedBy(username);
        req.setUpdatedBy(username);

        try {
            req.setUrgency(Urgency.valueOf(requestDto.getUrgency().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid urgency level: " + requestDto.getUrgency());
        }

        req.setRequestStatus(RequestStatus.PENDING);

        BloodRequest saved = bloodRequestRepository.save(req);

        // Publish event to RabbitMQ for downstream Notification Service
        try {
            eventPublisher.publishBloodRequestCreated(
                    saved.getId(),
                    saved.getHospitalId(),
                    hospital.getName(),
                    String.valueOf(saved.getBloodGroupId()),
                    saved.getComponentTypeCode(),
                    saved.getUnitsRequested(),
                    saved.getUrgency().name(),
                    null,
                    null
            );
        } catch (Exception ex) {
            log.error("Failed to publish BloodRequestCreatedEvent for requestId={}", saved.getId(), ex);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BloodRequestResponse approveRequest(Long id, String username) {
        log.info("Approving blood request ID: {} by user: {}", id, username);
        BloodRequest req = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with ID: " + id));

        if (req.getRequestStatus() != RequestStatus.PENDING) {
            throw new InvalidInputException("Cannot approve request from status: " + req.getRequestStatus());
        }

        req.setRequestStatus(RequestStatus.APPROVED);
        req.setUpdatedBy(username);
        BloodRequest saved = bloodRequestRepository.save(req);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BloodRequestResponse rejectRequest(Long id, String username) {
        log.info("Rejecting blood request ID: {} by user: {}", id, username);
        BloodRequest req = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with ID: " + id));

        if (req.getRequestStatus() != RequestStatus.PENDING) {
            throw new InvalidInputException("Cannot reject request from status: " + req.getRequestStatus());
        }

        req.setRequestStatus(RequestStatus.REJECTED);
        req.setUpdatedBy(username);
        BloodRequest saved = bloodRequestRepository.save(req);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BloodRequestResponse cancelRequest(Long id, Long userId, boolean isManager) {
        log.info("Processing cancel request for blood request ID: {} by user ID: {} (isManager={})", id, userId, isManager);

        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with ID: " + id));

        if (request.getRequestStatus() == RequestStatus.FULFILLED || request.getRequestStatus() == RequestStatus.CANCELLED) {
            throw new InvalidInputException("Blood request cannot be cancelled from current state: " + request.getRequestStatus());
        }

        if (!isManager && !request.getRequestedBy().equals(userId)) {
            throw new com.bloodbank.common.exception.UnauthorizedActionException("Only the original requesting user or an approving manager can cancel this blood request.");
        }

        request.setRequestStatus(RequestStatus.CANCELLED);
        request.setUpdatedBy("USER_" + userId);
        BloodRequest saved = bloodRequestRepository.save(request);

        log.info("Blood request ID {} successfully CANCELLED.", id);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public IssuanceResponse issueBlood(IssuanceRequest request) {
        log.info("Processing blood issuance for Request ID: {}, Blood Bank: {}, Units: {}",
                request.getBloodRequestId(), request.getBloodBankId(), request.getUnitsIssued());

        BloodRequest bloodRequest = bloodRequestRepository.findById(request.getBloodRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with ID: " + request.getBloodRequestId()));

        if (bloodRequest.getRequestStatus() != RequestStatus.APPROVED) {
            throw new InvalidInputException("Cannot issue blood for request in status: " + bloodRequest.getRequestStatus() 
                    + ". Request must be APPROVED first.");
        }

        String idempotencyKey = "ISSUANCE-DEC-" + bloodRequest.getId();
        BloodStockAdjustRequest adjustRequest = BloodStockAdjustRequest.builder()
                .bloodBankId(request.getBloodBankId())
                .bloodGroupId(bloodRequest.getBloodGroupId())
                .componentTypeCode(bloodRequest.getComponentTypeCode())
                .units(request.getUnitsIssued())
                .reason("Issuance for Request ID " + bloodRequest.getId())
                .referenceId(bloodRequest.getId())
                .idempotencyKey(idempotencyKey)
                .build();

        try {
            bloodBankServiceClient.decrementStock(adjustRequest);
        } catch (FeignException.BadRequest e) {
            log.error("Feign decrement call rejected due to bad request / insufficient stock.", e);
            String message = "Fulfillment failed: Insufficient stock in the selected blood bank. ";
            try {
                String body = e.contentUTF8();
                ApiResponse<?> apiResponse = objectMapper.readValue(body, ApiResponse.class);
                if (apiResponse != null && apiResponse.getMessage() != null) {
                    message += apiResponse.getMessage();
                }
            } catch (Exception ex) {
                message += "Units requested exceed available stock limits.";
            }
            throw new InvalidInputException(message);
        } catch (Exception e) {
            log.error("Failed S2S decrement stock call for request ID: {}", bloodRequest.getId(), e);
            throw new RuntimeException("Could not execute blood stock reduction. Issuance aborted.", e);
        }

        IssuanceRecord issuance = new IssuanceRecord();
        issuance.setBloodRequestId(bloodRequest.getId());
        issuance.setBloodBankId(request.getBloodBankId());
        issuance.setUnitsIssued(request.getUnitsIssued());
        issuance.setIssuedBy(request.getIssuedBy());
        issuance.setIssuedAt(Instant.now());
        issuance.setCrossMatchReference(request.getCrossMatchReference());
        issuance.setCreatedBy("STAFF_" + request.getIssuedBy());
        issuance.setUpdatedBy("STAFF_" + request.getIssuedBy());
        IssuanceRecord savedIssuance = issuanceRecordRepository.save(issuance);

        bloodRequest.setRequestStatus(RequestStatus.FULFILLED);
        bloodRequestRepository.save(bloodRequest);

        log.info("Issuance ID {} successfully finalized and request status updated to FULFILLED.", savedIssuance.getId());

        return mapToIssuanceResponse(savedIssuance);
    }

    @Override
    @Transactional(readOnly = true)
    public BloodRequestResponse getRequestById(Long id) {
        log.info("Fetching blood request ID: {}", id);
        BloodRequest req = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with ID: " + id));
        return mapToResponse(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BloodRequestResponse> searchRequests(Long hospitalId, Long bloodGroupId, String status, int page, int size) {
        log.info("Searching blood requests: hospitalId={} bloodGroupId={} status={}", hospitalId, bloodGroupId, status);

        RequestStatus requestStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                requestStatus = RequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid request status filter: " + status);
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        Page<BloodRequest> requestPage = bloodRequestRepository.searchRequests(hospitalId, bloodGroupId, requestStatus, pageable);

        return requestPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasHospitalRequests(Long hospitalId) {
        log.info("Checking if hospital ID {} has any blood requests", hospitalId);
        return bloodRequestRepository.existsByHospitalId(hospitalId);
    }

    private BloodRequestResponse mapToResponse(BloodRequest req) {
        return BloodRequestResponse.builder()
                .id(req.getId())
                .hospitalId(req.getHospitalId())
                .hospitalName(dataResolver.getHospitalName(req.getHospitalId()))
                .requestedBy(req.getRequestedBy())
                .bloodGroupId(req.getBloodGroupId())
                .bloodGroupLabel(dataResolver.getBloodGroupLabel(req.getBloodGroupId()))
                .componentTypeCode(req.getComponentTypeCode())
                .componentTypeLabel(dataResolver.getComponentTypeLabel(req.getComponentTypeCode()))
                .unitsRequested(req.getUnitsRequested())
                .urgency(req.getUrgency().name())
                .requestStatus(req.getRequestStatus().name())
                .patientName(req.getPatientName())
                .patientAge(req.getPatientAge())
                .clinicalReason(req.getClinicalReason())
                .requestedAt(req.getRequestedAt())
                .build();
    }

    private IssuanceResponse mapToIssuanceResponse(IssuanceRecord issuance) {
        return IssuanceResponse.builder()
                .id(issuance.getId())
                .bloodRequestId(issuance.getBloodRequestId())
                .bloodBankId(issuance.getBloodBankId())
                .bloodBankName(dataResolver.getBloodBankName(issuance.getBloodBankId()))
                .unitsIssued(issuance.getUnitsIssued())
                .issuedBy(issuance.getIssuedBy())
                .issuedAt(issuance.getIssuedAt())
                .crossMatchReference(issuance.getCrossMatchReference())
                .build();
    }
}
