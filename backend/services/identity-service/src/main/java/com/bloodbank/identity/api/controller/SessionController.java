package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.response.SessionDetailsResponse;
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
@Tag(name = "Active Device Sessions", description = "Simple endpoints for viewing logged-in devices and logging out specific or all other devices")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "View Active Logged-In Devices", description = "Get list of all devices currently logged into your account")
    public ResponseEntity<ApiResponse<List<SessionDetailsResponse>>> getUserSessions(Principal principal, HttpServletRequest request) {
        String currentSessionId = request.getHeader("X-Session-Id");
        List<SessionDetailsResponse> sessions = sessionService.getUserSessions(principal.getName(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success("Fetched user active sessions", sessions));
    }

    @GetMapping("/me")
    @Operation(summary = "View Current Device Details", description = "Get details of the device you are currently using")
    public ResponseEntity<ApiResponse<SessionDetailsResponse>> getCurrentSession(Principal principal, HttpServletRequest request) {
        String currentSessionId = request.getHeader("X-Session-Id");
        SessionDetailsResponse session = sessionService.getCurrentSession(principal.getName(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Logout Target Device", description = "Log out a specific device by session ID")
    public ResponseEntity<ApiResponse<Void>> terminateSession(@PathVariable String sessionId, Principal principal) {
        sessionService.terminateSession(principal.getName(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Target session terminated successfully", null));
    }

    @DeleteMapping
    @Operation(summary = "Logout All Other Devices", description = "Log out all other devices except your current one")
    public ResponseEntity<ApiResponse<Void>> logoutOtherSessions(Principal principal, HttpServletRequest request) {
        String currentSessionId = request.getHeader("X-Session-Id");
        sessionService.terminateOtherSessions(principal.getName(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success("All other sessions logged out successfully", null));
    }
}
