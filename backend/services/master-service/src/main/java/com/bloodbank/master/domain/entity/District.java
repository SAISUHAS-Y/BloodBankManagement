package com.bloodbank.master.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Entity
@Table(
    name = "districts",
    indexes = {
        @Index(name = "idx_district_state_id", columnList = "state_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class District extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        District district = (District) o;
        return getId() != null && Objects.equals(getId(), district.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
