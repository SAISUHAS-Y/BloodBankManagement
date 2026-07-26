package com.bloodbank.master.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Entity
@Table(
    name = "blood_groups",
    indexes = {
        @Index(name = "idx_blood_group_code", columnList = "code", unique = true)
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class BloodGroup extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 20)
    private String displayName;

    @Column(name = "is_universal_donor", nullable = false)
    private boolean isUniversalDonor;

    @Column(name = "is_universal_recipient", nullable = false)
    private boolean isUniversalRecipient;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodGroup that = (BloodGroup) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
