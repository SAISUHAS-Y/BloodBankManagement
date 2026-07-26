package com.bloodbank.common.core.context;

import java.util.UUID;

public class RequestContext {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    public static String getTraceId() {
        String traceId = TRACE_ID_HOLDER.get();
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            TRACE_ID_HOLDER.set(traceId);
        }
        return traceId;
    }

    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            TRACE_ID_HOLDER.set(traceId);
        } else {
            TRACE_ID_HOLDER.set(UUID.randomUUID().toString());
        }
    }

    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
