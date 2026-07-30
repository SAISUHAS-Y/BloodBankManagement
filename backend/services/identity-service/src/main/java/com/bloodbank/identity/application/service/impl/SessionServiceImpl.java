package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.identity.application.dto.response.SessionDetailsResponse;
import com.bloodbank.identity.application.service.SessionService;
import com.bloodbank.identity.domain.entity.User;
import com.bloodbank.identity.domain.entity.UserSession;
import com.bloodbank.identity.domain.repository.UserRepository;
import com.bloodbank.identity.domain.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SessionDetailsResponse> getUserSessions(String username, String currentSessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<UserSession> sessions = sessionRepository.findByUserIdAndActiveTrue(user.getId());
        return sessions.stream()
                .map(s -> mapToResponse(s, currentSessionId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDetailsResponse getCurrentSession(String username, String currentSessionId) {
        UserSession session = sessionRepository.findBySessionId(currentSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + currentSessionId));

        return mapToResponse(session, currentSessionId);
    }

    @Override
    @Transactional
    public void terminateSession(String username, String sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!Objects.equals(session.getUserId(), user.getId())) {
            throw new ResourceNotFoundException("Session not found for user: " + username);
        }

        session.setActive(false);
        sessionRepository.save(session);
        log.info("Terminated session [{}] for user [{}]", sessionId, username);
    }

    @Override
    @Transactional
    public void terminateOtherSessions(String username, String currentSessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<UserSession> sessions = sessionRepository.findByUserIdAndActiveTrue(user.getId());
        sessions.forEach(s -> {
            if (!Objects.equals(s.getSessionId(), currentSessionId)) {
                s.setActive(false);
            }
        });
        sessionRepository.saveAll(sessions);
        log.info("Terminated all other active sessions for user [{}] except [{}]", username, currentSessionId);
    }

    private SessionDetailsResponse mapToResponse(UserSession session, String currentSessionId) {
        return SessionDetailsResponse.builder()
                .sessionId(session.getSessionId())
                .deviceId(session.getDeviceId())
                .deviceName(session.getDeviceName())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .isCurrentSession(Objects.equals(session.getSessionId(), currentSessionId))
                .createdAt(session.getCreatedAt())
                .lastActivityAt(session.getLastActivityAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
