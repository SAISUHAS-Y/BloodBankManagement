package com.bloodbank.identity.domain.repository;

import com.bloodbank.identity.domain.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    List<UserSession> findByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}
