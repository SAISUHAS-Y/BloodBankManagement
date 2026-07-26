package com.bloodbank.common.core.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

    // Correlation and Trace ID Constants
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_CORRELATION_ID_KEY = "correlationId";

    // Pagination Defaults
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";
    public static final String DEFAULT_SORT_BY = "id";

    // Date and Time Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_ZONE_UTC = "UTC";

    // Security & Auth Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // Standard Messaging Headers
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USER_PERMISSIONS_HEADER = "X-User-Permissions";
}
