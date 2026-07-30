package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.response.SessionDetailsResponse;

import java.util.List;

public interface SessionService {

    List<SessionDetailsResponse> getUserSessions(String username, String currentSessionId);

    SessionDetailsResponse getCurrentSession(String username, String currentSessionId);

    void terminateSession(String username, String sessionId);

    void terminateOtherSessions(String username, String currentSessionId);
}
