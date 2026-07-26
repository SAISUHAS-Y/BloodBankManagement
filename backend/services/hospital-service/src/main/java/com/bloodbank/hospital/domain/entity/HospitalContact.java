package com.bloodbank.hospital.domain.entity;

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
    name = "hospital_contacts",
    indexes = {
        @Index(name = "idx_contact_hospital_id", columnList = "hospital_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class HospitalContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HospitalContact contact = (HospitalContact) o;
        return getId() != null && Objects.equals(getId(), contact.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
