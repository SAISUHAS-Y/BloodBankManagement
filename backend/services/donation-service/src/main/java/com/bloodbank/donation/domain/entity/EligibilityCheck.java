package com.bloodbank.donation.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
    name = "eligibility_checks",
    indexes = {
        @Index(name = "idx_eligibility_donor", columnList = "donor_profile_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class EligibilityCheck extends BaseEntity {

    @Column(name = "donor_profile_id", nullable = false)
    private Long donorProfileId;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt = Instant.now();

    @Column(name = "hemoglobin_level", nullable = false)
    private double hemoglobinLevel;

    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @Column(name = "systolic_bp", nullable = false)
    private double systolicBp;

    @Column(name = "diastolic_bp", nullable = false)
    private double diastolicBp;

    @Column(name = "is_eligible", nullable = false)
    private boolean isEligible;

    @Column(name = "deferral_reason_code", length = 50)
    private String deferralReasonCode;

    @Column(name = "deferred_until_date")
    private LocalDate deferredUntilDate;

    @Column(name = "checked_by", nullable = false)
    private Long checkedBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EligibilityCheck that = (EligibilityCheck) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
