package com.bloodbank.common.security.config;

import com.bloodbank.common.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Default Security Filter Chain shared across all servlet-based microservices.
 * 
 * This bean activates ONLY when no other SecurityFilterChain bean is defined by the 
 * consuming microservice (via @ConditionalOnMissingBean). Services that need custom 
 * security rules (e.g., identity-service) can define their own SecurityFilterChain 
 * bean, which takes priority and causes this default to be skipped.
 * 
 * Default behavior:
 * - Disables CSRF (stateless JWT architecture)
 * - Stateless session management
 * - Permits: Swagger UI, OpenAPI docs, Actuator endpoints, and webjars
 * - Requires authentication for all other paths
 * - Adds the shared JWT authentication filter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class DefaultSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI documentation paths
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                // Spring Boot Actuator health and info endpoints
                .requestMatchers("/actuator/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
