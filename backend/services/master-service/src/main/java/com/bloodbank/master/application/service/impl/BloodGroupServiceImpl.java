package com.bloodbank.master.application.service.impl;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.master.application.dto.BloodGroupResponse;
import com.bloodbank.master.application.service.BloodGroupService;
import com.bloodbank.master.domain.entity.BloodGroup;
import com.bloodbank.master.domain.repository.BloodGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodGroupServiceImpl implements BloodGroupService {

    private final BloodGroupRepository bloodGroupRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "blood_groups")
    public List<BloodGroupResponse> getAllBloodGroups() {
        log.info("Fetching blood groups from database");
        return bloodGroupRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "blood_groups", key = "#code")
    public BloodGroupResponse getBloodGroupByCode(String code) {
        log.info("Fetching single blood group from database for code: {}", code);
        BloodGroup bg = bloodGroupRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with code: " + code));
        return mapToResponse(bg);
    }

    private BloodGroupResponse mapToResponse(BloodGroup bg) {
        return BloodGroupResponse.builder()
                .id(bg.getId())
                .code(bg.getCode())
                .displayName(bg.getDisplayName())
                .universalDonor(bg.isUniversalDonor())
                .universalRecipient(bg.isUniversalRecipient())
                .build();
    }
}
