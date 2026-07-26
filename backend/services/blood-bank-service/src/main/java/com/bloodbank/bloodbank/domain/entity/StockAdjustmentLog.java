package com.bloodbank.bloodbank.domain.entity;

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
    name = "stock_adjustment_logs",
    indexes = {
        @Index(name = "idx_adj_stock_id", columnList = "blood_stock_id"),
        @Index(name = "idx_adj_user_id", columnList = "adjusted_by")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class StockAdjustmentLog extends BaseEntity {

    @Column(name = "blood_stock_id", nullable = false)
    private Long bloodStockId;

    @Column(name = "previous_units", nullable = false)
    private double previousUnits;

    @Column(name = "new_units", nullable = false)
    private double newUnits;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "adjusted_by", nullable = false)
    private Long adjustedBy;

    @Column(name = "adjusted_at", nullable = false)
    private Instant adjustedAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockAdjustmentLog log = (StockAdjustmentLog) o;
        return getId() != null && Objects.equals(getId(), log.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
