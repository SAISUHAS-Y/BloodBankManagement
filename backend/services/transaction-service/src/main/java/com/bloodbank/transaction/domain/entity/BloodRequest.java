package com.bloodbank.transaction.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import com.bloodbank.transaction.domain.enums.RequestStatus;
import com.bloodbank.transaction.domain.enums.Urgency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "blood_requests",
    indexes = {
        @Index(name = "idx_request_hospital", columnList = "hospital_id"),
        @Index(name = "idx_request_group", columnList = "blood_group_id"),
        @Index(name = "idx_request_status", columnList = "request_status")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class BloodRequest extends BaseEntity {

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "blood_group_id", nullable = false)
    private Long bloodGroupId;

    @Column(name = "component_type_code", nullable = false, length = 50)
    private String componentTypeCode;

    @Column(name = "units_requested", nullable = false)
    private double unitsRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false, length = 50)
    private Urgency urgency = Urgency.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 50)
    private RequestStatus requestStatus = RequestStatus.PENDING;

    @Column(name = "patient_name", nullable = false, length = 150)
    private String patientName;

    @Column(name = "patient_age", nullable = false)
    private int patientAge;

    @Column(name = "clinical_reason", length = 255)
    private String clinicalReason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @jakarta.persistence.Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodRequest that = (BloodRequest) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
