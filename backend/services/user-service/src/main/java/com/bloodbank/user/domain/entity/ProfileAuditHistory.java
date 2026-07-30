package com.bloodbank.user.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "profile_audit_history",
    indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class ProfileAuditHistory extends BaseEntity {

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // "DONOR" or "STAFF"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // "STATUS_CHANGE", "FIELD_UPDATE", "TRANSFER"

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "changed_by", nullable = false, length = 150)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileAuditHistory that = (ProfileAuditHistory) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
