package com.bloodbank.master.api.config;

import com.bloodbank.master.domain.entity.BloodGroup;
import com.bloodbank.master.domain.repository.BloodGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterDataSeeder implements CommandLineRunner {

    private final BloodGroupRepository bloodGroupRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding Master Blood Group records...");

        List<BloodGroup> seedList = List.of(
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

        for (BloodGroup bg : seedList) {
            if (bloodGroupRepository.findByCode(bg.getCode()).isEmpty()) {
                bloodGroupRepository.save(bg);
                log.info("Seeded missing master blood group: {} ({})", bg.getCode(), bg.getDisplayName());
            }
        }
    }

    private BloodGroup createBloodGroup(String code, String displayName, boolean universalDonor, boolean universalRecipient) {
        BloodGroup bg = new BloodGroup();
        bg.setCode(code);
        bg.setDisplayName(displayName);
        bg.setUniversalDonor(universalDonor);
        bg.setUniversalRecipient(universalRecipient);
        return bg;
    }
}
