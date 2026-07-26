package com.bloodbank.donation.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import com.bloodbank.donation.domain.enums.DonationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
    name = "donations",
    indexes = {
        @Index(name = "idx_donation_donor", columnList = "donor_profile_id"),
        @Index(name = "idx_donation_bb", columnList = "blood_bank_id"),
        @Index(name = "idx_donation_group", columnList = "blood_group_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class DonationRecord extends BaseEntity {

    @Column(name = "donor_profile_id", nullable = false)
    private Long donorProfileId;

    @Column(name = "blood_bank_id", nullable = false)
    private Long bloodBankId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eligibility_check_id", nullable = false)
    private EligibilityCheck eligibilityCheck;

    @Column(name = "blood_group_id", nullable = false)
    private Long bloodGroupId;

    @Column(name = "component_type_code", nullable = false, length = 50)
    private String componentTypeCode;

    @Column(name = "units_collected", nullable = false)
    private double unitsCollected;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_status", nullable = false, length = 50)
    private DonationStatus donationStatus = DonationStatus.SCHEDULED;

    @Column(name = "collected_by")
    private Long collectedBy;

    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;

    @Column(name = "notes", length = 255)
    private String notes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonationRecord that = (DonationRecord) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
