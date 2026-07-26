package com.bloodbank.user.domain.entity;

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
@Table(name = "processed_donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedDonation {

    @Id
    @Column(name = "donation_id")
    private Long donationId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();
}
