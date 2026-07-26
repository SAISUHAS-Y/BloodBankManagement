package com.bloodbank.gateway.filter;

import com.bloodbank.common.core.constant.AppConstants;
import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalJwtValidationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/actuator",
            "/fallback",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. Trace ID propagation
        String traceId = request.getHeaders().getFirst(AppConstants.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }

        // 2. Allow CORS Preflight OPTIONS requests without JWT validation
        if (CorsUtils.isPreFlightRequest(request) || HttpMethod.OPTIONS.equals(request.getMethod())) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(AppConstants.TRACE_ID_HEADER, traceId)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 3. Bypass security checks ONLY for explicitly whitelisted public paths
        if (isExcluded(path)) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(AppConstants.TRACE_ID_HEADER, traceId)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 4. Extract and validate Authorization Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(AppConstants.TOKEN_PREFIX)) {
            log.warn("Missing or invalid Authorization header for request path: {}", path);
            return onError(exchange, "Missing or invalid Authorization token", HttpStatus.UNAUTHORIZED, traceId);
        }

        String token = authHeader.substring(AppConstants.TOKEN_PREFIX.length()).trim();

        if (!tokenProvider.validateToken(token)) {
            log.warn("JWT Token validation failed for request path: {}", path);
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED, traceId);
        }

        try {
            Long userId = tokenProvider.getUserIdFromToken(token);
            Collection<? extends GrantedAuthority> authorities = tokenProvider.getAuthoritiesFromToken(token);

            List<String> roles = (authorities != null) ? authorities.stream()
                    .map(auth -> auth.getAuthority())
                    .filter(auth -> auth != null && auth.startsWith("ROLE_"))
                    .map(auth -> auth.replace("ROLE_", ""))
                    .toList() : List.of();

            List<String> permissions = (authorities != null) ? authorities.stream()
                    .map(auth -> auth.getAuthority())
                    .filter(auth -> auth != null && !auth.startsWith("ROLE_"))
                    .toList() : List.of();

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(AppConstants.TRACE_ID_HEADER, traceId)
                    .header(AppConstants.USER_ID_HEADER, String.valueOf(userId))
                    .header(AppConstants.USER_ROLES_HEADER, String.join(",", roles))
                    .header(AppConstants.USER_PERMISSIONS_HEADER, String.join(",", permissions))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception ex) {
            log.error("Error extracting JWT claims for path: {}", path, ex);
            return onError(exchange, "Error parsing JWT claims", HttpStatus.UNAUTHORIZED, traceId);
        }
    }

    private boolean isExcluded(String path) {
        if (path == null) {
            return false;
        }
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus httpStatus, String traceId) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ApiResponse<Void> apiResponse = ApiResponse.failure(httpStatus.value(), errMessage, traceId);
            byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception ex) {
            log.error("Failed to write JSON error response in GlobalJwtValidationFilter", ex);
            byte[] fallbackBytes = String.format("{\"success\":false,\"status\":%d,\"message\":\"%s\",\"data\":null,\"traceId\":\"%s\"}",
                    httpStatus.value(), errMessage, traceId).getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallbackBytes)));
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
