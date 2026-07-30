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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodGroupServiceImpl implements BloodGroupService {

    private final BloodGroupRepository bloodGroupRepository;

    @Override
    @Transactional
    public List<BloodGroupResponse> getAllBloodGroups() {
        log.info("Fetching blood groups from database");
        List<BloodGroup> list = bloodGroupRepository.findAll();

        if (list.isEmpty()) {
            log.info("No blood groups found in database. Initializing default master records...");
            list = seedDefaultBloodGroups();
        }

        return list.stream()
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

    private List<BloodGroup> seedDefaultBloodGroups() {
        List<BloodGroup> defaults = List.of(
            createBloodGroup("UNKNOWN", "Unknown", false, false),
            createBloodGroup("O_POSITIVE", "O+", false, false),
            createBloodGroup("O_NEGATIVE", "O-", true, false),
            createBloodGroup("A_POSITIVE", "A+", false, false),
            createBloodGroup("A_NEGATIVE", "A-", false, false),
            createBloodGroup("B_POSITIVE", "B+", false, false),
            createBloodGroup("B_NEGATIVE", "B-", false, false),
            createBloodGroup("AB_POSITIVE", "AB+", false, true),
            createBloodGroup("AB_NEGATIVE", "AB-", false, false)
        );

        List<BloodGroup> savedList = new ArrayList<>();
        for (BloodGroup bg : defaults) {
            savedList.add(bloodGroupRepository.save(bg));
        }
        return savedList;
    }

    private BloodGroup createBloodGroup(String code, String displayName, boolean universalDonor, boolean universalRecipient) {
        BloodGroup bg = new BloodGroup();
        bg.setCode(code);
        bg.setDisplayName(displayName);
        bg.setUniversalDonor(universalDonor);
        bg.setUniversalRecipient(universalRecipient);
        return bg;
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
