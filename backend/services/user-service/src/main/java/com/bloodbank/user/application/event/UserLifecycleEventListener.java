package com.bloodbank.user.application.event;

import com.bloodbank.common.events.EventEnvelope;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.entity.StaffProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.enums.StaffStatus;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import com.bloodbank.user.domain.repository.StaffProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserLifecycleEventListener {

    private final DonorProfileRepository donorProfileRepository;
    private final StaffProfileRepository staffProfileRepository;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(name = "user.profile.identity.lifecycle.queue", durable = "true"),
            exchange = @Exchange(name = "identity.exchange", type = "topic", durable = "true"),
            key = "user.deactivated"
        )
    )
    @Transactional
    public void handleUserDeactivated(EventEnvelope<Map<String, Object>> envelope) {
        Map<String, Object> payload = envelope.getPayload();
        log.info("Received identity user deactivation event: payload={}", payload);

        if (payload == null || !payload.containsKey("userId")) {
            log.warn("Invalid payload received for identity user deactivation");
            return;
        }

        Long identityUserId = Long.valueOf(payload.get("userId").toString());

        // Sync linked donor profile
        Optional<DonorProfile> donorOpt = donorProfileRepository.findByIdentityUserId(identityUserId);
        if (donorOpt.isPresent()) {
            DonorProfile donor = donorOpt.get();
            log.info("Cascading user deactivation to donor profile ID: {}", donor.getId());
            donor.setDonorStatus(DonorStatus.DEFERRED);
            donor.setUpdatedBy("EVENT_CASCADE");
            donorProfileRepository.save(donor);
        }

        // Sync linked staff profile
        Optional<StaffProfile> staffOpt = staffProfileRepository.findByIdentityUserId(identityUserId);
        if (staffOpt.isPresent()) {
            StaffProfile staff = staffOpt.get();
            log.info("Cascading user deactivation to staff profile ID: {}", staff.getId());
            staff.setStaffStatus(StaffStatus.SUSPENDED);
            staff.setUpdatedBy("EVENT_CASCADE");
            staffProfileRepository.save(staff);
        }
    }
}
