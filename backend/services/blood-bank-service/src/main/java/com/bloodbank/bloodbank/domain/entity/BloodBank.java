package com.bloodbank.bloodbank.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
    name = "blood_banks",
    indexes = {
        @Index(name = "idx_bb_license_number", columnList = "license_number", unique = true),
        @Index(name = "idx_bb_location", columnList = "state_id, district_id, city_id"),
        @Index(name = "idx_bb_email", columnList = "email")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class BloodBank extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @Column(name = "blood_bank_type_code", nullable = false, length = 50)
    private String bloodBankTypeCode;

    @Column(name = "state_id", nullable = false)
    private Long stateId;

    @Column(name = "district_id", nullable = false)
    private Long districtId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "operating_hours_note", length = 255)
    private String operatingHoursNote;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "bloodBank", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BloodStock> stocks = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodBank bloodBank = (BloodBank) o;
        return getId() != null && Objects.equals(getId(), bloodBank.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
