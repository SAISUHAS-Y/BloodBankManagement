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
    name = "lookup_items",
    indexes = {
        @Index(name = "idx_lookup_item_cat_code", columnList = "category_id, code", unique = true),
        @Index(name = "idx_lookup_item_code", columnList = "code")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class LookupItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private LookupCategory category;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LookupItem that = (LookupItem) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
