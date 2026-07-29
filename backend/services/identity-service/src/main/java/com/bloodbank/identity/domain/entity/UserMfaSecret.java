package com.bloodbank.identity.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "user_mfa_secrets")
@Getter
@Setter
public class UserMfaSecret extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "secret_key", nullable = false, length = 128)
    private String secretKey;

    @Column(name = "is_mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_type", nullable = false, length = 20)
    private String mfaType = "TOTP";

    @Column(name = "scratch_codes", columnDefinition = "TEXT")
    private String scratchCodes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMfaSecret that = (UserMfaSecret) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
