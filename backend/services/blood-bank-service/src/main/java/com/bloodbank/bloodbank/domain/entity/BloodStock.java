package com.bloodbank.bloodbank.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "blood_stocks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bb_group_comp", columnNames = {"blood_bank_id", "blood_group_id", "component_type_code"})
    },
    indexes = {
        @Index(name = "idx_stock_bb", columnList = "blood_bank_id"),
        @Index(name = "idx_stock_group", columnList = "blood_group_id"),
        @Index(name = "idx_stock_component", columnList = "component_type_code")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class BloodStock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Column(name = "blood_group_id", nullable = false)
    private Long bloodGroupId;

    @Column(name = "component_type_code", nullable = false, length = 50)
    private String componentTypeCode;

    @Column(name = "units_available", nullable = false)
    private double unitsAvailable = 0.0;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodStock stock = (BloodStock) o;
        return getId() != null && Objects.equals(getId(), stock.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
