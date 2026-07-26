package com.bloodbank.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class GlobalRequestLoggingFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        final String finalTraceId = traceId;

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            long latency = System.currentTimeMillis() - startTime;
            var statusCode = mutatedExchange.getResponse().getStatusCode();
            int status = statusCode != null ? statusCode.value() : 500;

            log.info("[GATEWAY REQUEST] Method: {}, Path: {}, Status: {}, Latency: {}ms, TraceId: {}",
                    mutatedRequest.getMethod(),
                    mutatedRequest.getPath(),
                    status,
                    latency,
                    finalTraceId);
        }));
    }

    @Override
    public int getOrder() {
        return -200; // Execute before GlobalJwtValidationFilter (-100) to propagate traceId early
    }
}
