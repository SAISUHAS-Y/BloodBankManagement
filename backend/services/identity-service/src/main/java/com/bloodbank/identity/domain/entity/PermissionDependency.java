package com.bloodbank.identity.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "permission_dependencies")
@Getter
@Setter
public class PermissionDependency extends BaseEntity {

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Column(name = "depends_on_permission_id", nullable = false)
    private Long dependsOnPermissionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionDependency that = (PermissionDependency) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
