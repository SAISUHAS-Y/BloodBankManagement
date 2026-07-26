package com.bloodbank.gateway.config;

import com.bloodbank.common.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Declares bean dependencies from shared modules.
 * 
 * Imports {@link JwtTokenProvider} into the Gateway Application Context so `@Value` 
 * property injection and `@PostConstruct` initialization execute automatically.
 */
@Configuration
@Import(JwtTokenProvider.class)
public class GatewaySecurityConfig {
}
