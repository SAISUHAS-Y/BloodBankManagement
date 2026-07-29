package com.bloodbank.identity.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "blacklisted_ips")
@Getter
@Setter
public class BlacklistedIp extends BaseEntity {

    @Column(name = "ip_address", nullable = false, unique = true, length = 45)
    private String ipAddress;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "blocked_by", length = 50)
    private String blockedBy = "SYSTEM";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlacklistedIp that = (BlacklistedIp) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
