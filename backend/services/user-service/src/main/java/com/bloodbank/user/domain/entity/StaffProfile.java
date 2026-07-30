package com.bloodbank.user.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import com.bloodbank.user.domain.enums.StaffStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Entity
@Table(
    name = "staff_profiles",
    indexes = {
        @Index(name = "idx_staff_identity_user", columnList = "identity_user_id", unique = true),
        @Index(name = "idx_staff_blood_bank", columnList = "blood_bank_id"),
        @Index(name = "idx_staff_hospital", columnList = "hospital_id"),
        @Index(name = "idx_staff_status", columnList = "staff_status"),
        @Index(name = "idx_staff_reporting_manager", columnList = "reporting_manager_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class StaffProfile extends BaseEntity {

    @Column(name = "identity_user_id", nullable = false, unique = true)
    private Long identityUserId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "blood_bank_id")
    private Long bloodBankId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_status", nullable = false, length = 50)
    private StaffStatus staffStatus = StaffStatus.ACTIVE;

    @Column(name = "reporting_manager_id")
    private Long reportingManagerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffProfile that = (StaffProfile) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
