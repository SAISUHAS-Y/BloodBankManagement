package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.BlacklistedIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface BlacklistedIpRepository extends JpaRepository<BlacklistedIp, Long> {

    Optional<BlacklistedIp> findByIpAddress(String ipAddress);

    boolean existsByIpAddressAndExpiresAtAfter(String ipAddress, Instant now);

    boolean existsByIpAddressAndExpiresAtIsNull(String ipAddress);
}
