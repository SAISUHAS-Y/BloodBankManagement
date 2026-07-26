package com.bloodbank.transaction.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "issuance_records",
    indexes = {
        @Index(name = "idx_issuance_request", columnList = "blood_request_id"),
        @Index(name = "idx_issuance_bb", columnList = "blood_bank_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class IssuanceRecord extends BaseEntity {

    @Column(name = "blood_request_id", nullable = false)
    private Long bloodRequestId;

    @Column(name = "blood_bank_id", nullable = false)
    private Long bloodBankId;

    @Column(name = "units_issued", nullable = false)
    private double unitsIssued;

    @Column(name = "issued_by", nullable = false)
    private Long issuedBy;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "cross_match_reference", length = 150)
    private String crossMatchReference;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssuanceRecord that = (IssuanceRecord) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
