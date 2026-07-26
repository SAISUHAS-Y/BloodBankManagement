package com.bloodbank.user.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import com.bloodbank.user.domain.enums.DonorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
    name = "donor_profiles",
    indexes = {
        @Index(name = "idx_donor_identity_user", columnList = "identity_user_id"),
        @Index(name = "idx_donor_blood_group", columnList = "blood_group_id"),
        @Index(name = "idx_donor_location", columnList = "state_id, district_id, city_id"),
        @Index(name = "idx_donor_email", columnList = "email"),
        @Index(name = "idx_donor_phone", columnList = "phone")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class DonorProfile extends BaseEntity {

    @Column(name = "identity_user_id")
    private Long identityUserId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "gender_code", nullable = false, length = 50)
    private String genderCode;

    @Column(name = "blood_group_id", nullable = false)
    private Long bloodGroupId;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "state_id", nullable = false)
    private Long stateId;

    @Column(name = "district_id", nullable = false)
    private Long districtId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "id_type", length = 50)
    private String idType;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "donor_status", nullable = false, length = 50)
    private DonorStatus donorStatus = DonorStatus.ACTIVE;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "deferred_until_date")
    private LocalDate deferredUntilDate;

    @Column(name = "total_donations", nullable = false)
    private int totalDonations = 0;

    @Column(name = "last_processed_donation_id", unique = true)
    private Long lastProcessedDonationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonorProfile that = (DonorProfile) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
