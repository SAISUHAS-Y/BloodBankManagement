package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.UserMfaSecret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMfaSecretRepository extends JpaRepository<UserMfaSecret, Long> {

    Optional<UserMfaSecret> findByUserId(Long userId);

    boolean existsByUserIdAndMfaEnabledTrue(Long userId);
}
