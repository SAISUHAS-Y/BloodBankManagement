package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.SessionDetailsResponse;
import com.bloodbank.identity.application.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Governance", description = "Endpoints for inspecting and remotely terminating active device sessions")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "Get All Active Sessions", description = "Lists all active sessions across all registered devices for the current authenticated user")
    public ResponseEntity<ApiResponse<List<SessionDetailsResponse>>> getUserSessions(Principal principal, HttpServletRequest request) {
        String currentSessionId = request.getHeader("X-Session-Id");
        List<SessionDetailsResponse> sessions = sessionService.getUserSessions(principal.getName(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success("Fetched user active sessions", sessions));
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current Session Metadata", description = "Returns metadata for the currently active session processing this request")
    public ResponseEntity<ApiResponse<SessionDetailsResponse>> getCurrentSession(Principal principal, HttpServletRequest request) {
        String currentSessionId = request.getHeader("X-Session-Id");
        SessionDetailsResponse session = sessionService.getCurrentSession(principal.getName(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Terminate Target Session", description = "Remotely invalidates a specific target active session by session ID")
    public ResponseEntity<ApiResponse<Void>> terminateSession(@PathVariable String sessionId, Principal principal) {
        sessionService.terminateSession(principal.getName(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Target session terminated successfully", null));
    }

    @DeleteMapping
    @Operation(summary = "Logout Other Sessions", description = "Terminates all active sessions for the user EXCEPT the current processing session")
    public ResponseEntity<ApiResponse<Void>> logoutOtherSessions(Principal principal, HttpServletRequest request) {
        String currentSessionId = request.getHeader("X-Session-Id");
        sessionService.terminateOtherSessions(principal.getName(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success("All other sessions logged out successfully", null));
    }
}
