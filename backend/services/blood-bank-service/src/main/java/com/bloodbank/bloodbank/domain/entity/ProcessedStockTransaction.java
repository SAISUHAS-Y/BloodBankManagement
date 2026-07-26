package com.bloodbank.bloodbank.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "processed_stock_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedStockTransaction {

    @Id
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();
}
