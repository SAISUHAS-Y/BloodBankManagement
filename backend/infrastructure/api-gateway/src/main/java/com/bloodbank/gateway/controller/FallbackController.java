package com.bloodbank.gateway.controller;

import com.bloodbank.common.core.constant.AppConstants;
import com.bloodbank.common.core.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/service-unavailable")
    public Mono<ResponseEntity<ApiResponse<Void>>> handleServiceUnavailable(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(AppConstants.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        ApiResponse<Void> response = ApiResponse.failure(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service temporarily unavailable. Please try again later.",
                traceId
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
