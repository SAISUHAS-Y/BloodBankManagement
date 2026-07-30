package com.bloodbank.identity.application.service;

import com.bloodbank.common.core.dto.PageResponse;
import com.bloodbank.identity.application.dto.request.AuthAuditLogQueryRequest;
import com.bloodbank.identity.application.dto.response.SecurityAnalyticsSummaryResponse;
import com.bloodbank.identity.domain.entity.AuthAuditLog;

public interface SecurityAnalyticsService {

    PageResponse<AuthAuditLog> searchAuditLogs(AuthAuditLogQueryRequest request);

    SecurityAnalyticsSummaryResponse getAnalyticsSummary();
}
