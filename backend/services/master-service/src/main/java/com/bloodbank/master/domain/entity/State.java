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
    name = "states",
    indexes = {
        @Index(name = "idx_state_code", columnList = "code", unique = true)
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class State extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return getId() != null && Objects.equals(getId(), state.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
