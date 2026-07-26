package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHashAndTokenType(String tokenHash, String tokenType);

    Optional<VerificationToken> findByTokenHash(String tokenHash);
}
