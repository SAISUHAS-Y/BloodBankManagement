package com.bloodbank.common.contracts.config;

import com.bloodbank.common.core.context.RequestContext;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {

    /**
     * Timeout configuration justification:
     * - Connect Timeout (3s): Microservice-to-microservice calls over internal network should establish connection fast.
     *   3 seconds quickly catches unresponsive or crashed instances without waiting endlessly.
     * - Read Timeout (5s): Gives downstream services enough time to process DB queries while protecting caller thread
     *   pools from starvation.
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                3, TimeUnit.SECONDS,
                5, TimeUnit.SECONDS,
                true
        );
    }

    /**
     * Propagate traceId across service boundaries via HTTP header.
     */
    @Bean
    public RequestInterceptor traceIdRequestInterceptor() {
        return requestTemplate -> {
            String traceId = RequestContext.getTraceId();
            if (traceId != null && !traceId.isBlank()) {
                requestTemplate.header(RequestContext.TRACE_ID_HEADER, traceId);
            }
        };
    }
}
