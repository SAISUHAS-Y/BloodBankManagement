package com.bloodbank.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

@Configuration
public class RouteConfig {

        @Value("${app.rate-limiter.auth-replenish-rate:5}")
        private int authReplenishRate;

        @Value("${app.rate-limiter.auth-burst-capacity:5}")
        private int authBurstCapacity;

        @Value("${app.rate-limiter.default-replenish-rate:20}")
        private int defaultReplenishRate;

        @Value("${app.rate-limiter.default-burst-capacity:40}")
        private int defaultBurstCapacity;

        @Bean
        public RedisRateLimiter authRateLimiter() {
                return new RedisRateLimiter(authReplenishRate, authBurstCapacity);
        }

        @Bean
        @Primary
        public RedisRateLimiter defaultRateLimiter() {
                return new RedisRateLimiter(defaultReplenishRate, defaultBurstCapacity);
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                        RedisRateLimiter authRateLimiter,
                        RedisRateLimiter defaultRateLimiter) {
                return builder.routes()
                                // Route 1: Auth Endpoints (Strict Rate Limited to 5 req/min per IP)
                                .route("auth-service-strict", r -> r
                                                .path("/api/v1/auth/login", "/api/v1/auth/refresh")
                                                .and().method(HttpMethod.POST)
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(authRateLimiter)
                                                                                .setKeyResolver(ipKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("authCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://identity-service"))

                                // Route 1b: Identity Service General APIs
                                .route("identity-service", r -> r
                                                .path("/api/v1/admin/**", "/api/v1/users/**", "/api/v1/auth/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("identityCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://identity-service"))

                                // Route 2: Master Service
                                .route("master-service", r -> r
                                                .path("/api/v1/master/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("masterCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://master-service"))

                                // Route 3: User Service
                                .route("user-service", r -> r
                                                .path("/api/v1/donors/**", "/api/v1/staff/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("userCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://user-service"))

                                // Route 4: Hospital Service
                                .route("hospital-service", r -> r
                                                .path("/api/v1/hospitals/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("hospitalCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://hospital-service"))

                                // Route 5: Blood Bank Service
                                .route("blood-bank-service", r -> r
                                                .path("/api/v1/blood-banks/**", "/api/v1/blood-stocks/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("bloodBankCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://blood-bank-service"))

                                // Route 6: Donation Service
                                .route("donation-service", r -> r
                                                .path("/api/v1/donations/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("donationCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://donation-service"))

                                // Route 7: Transaction Service
                                .route("transaction-service", r -> r
                                                .path("/api/v1/blood-requests/**", "/api/v1/issuances/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("transactionCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://transaction-service"))

                                // Route 8: Notification Service
                                .route("notification-service", r -> r
                                                .path("/api/v1/notifications/**")
                                                .filters(f -> f
                                                                .requestRateLimiter(rl -> rl
                                                                                .setRateLimiter(defaultRateLimiter)
                                                                                .setKeyResolver(userKeyResolver()))
                                                                .circuitBreaker(cb -> cb
                                                                                .setName("notificationCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback/service-unavailable")))
                                                .uri("lb://notification-service"))

                                // Swagger UI API-Docs Proxies
                                .route("api-docs-identity", r -> r
                                                .path("/v3/api-docs/identity-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/identity-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://identity-service"))
                                .route("api-docs-master", r -> r
                                                .path("/v3/api-docs/master-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/master-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://master-service"))
                                .route("api-docs-user", r -> r
                                                .path("/v3/api-docs/user-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/user-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://user-service"))
                                .route("api-docs-hospital", r -> r
                                                .path("/v3/api-docs/hospital-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/hospital-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://hospital-service"))
                                .route("api-docs-blood-bank", r -> r
                                                .path("/v3/api-docs/blood-bank-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/blood-bank-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://blood-bank-service"))
                                .route("api-docs-donation", r -> r
                                                .path("/v3/api-docs/donation-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/donation-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://donation-service"))
                                .route("api-docs-transaction", r -> r
                                                .path("/v3/api-docs/transaction-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/transaction-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://transaction-service"))
                                .route("api-docs-notification", r -> r
                                                .path("/v3/api-docs/notification-service")
                                                .filters(f -> f.rewritePath("/v3/api-docs/notification-service",
                                                                "/v3/api-docs"))
                                                .uri("lb://notification-service"))
                                .build();
        }

        @Bean
        public KeyResolver ipKeyResolver() {
                return exchange -> {
                        var remoteAddr = exchange.getRequest().getRemoteAddress();
                        if (remoteAddr != null && remoteAddr.getAddress() != null) {
                                return Mono.just(remoteAddr.getAddress().getHostAddress());
                        }
                        return Mono.just("127.0.0.1");
                };
        }

        @Bean
        @Primary
        public KeyResolver userKeyResolver() {
                return exchange -> {
                        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                        if (userId != null && !userId.isBlank()) {
                                return Mono.just(userId);
                        }
                        var remoteAddr = exchange.getRequest().getRemoteAddress();
                        if (remoteAddr != null && remoteAddr.getAddress() != null) {
                                return Mono.just(remoteAddr.getAddress().getHostAddress());
                        }
                        return Mono.just("anonymous");
                };
        }
}
