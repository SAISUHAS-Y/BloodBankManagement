package com.bloodbank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

/**
 * Swagger/OpenAPI handler for the API Gateway.
 * 
 * In Spring Cloud Gateway, the gateway's RoutePredicateHandlerMapping takes
 * priority over WebFlux RouterFunctions. When /swagger-ui.html doesn't match
 * any gateway route, the request fails with 503 instead of reaching the
 * springdoc-openapi auto-configured handlers.
 * 
 * This configuration provides explicit WebFlux RouterFunctions to serve as
 * the entry points for the Swagger UI, ensuring they are properly registered
 * in the application context and can handle requests that bypass gateway routing.
 */
@Configuration
public class SwaggerConfig {

    private static final String SWAGGER_CONFIG_JSON = """
            {
              "configUrl": "/v3/api-docs/swagger-config",
              "urls": [
                {"name": "1. Identity Service (Auth & Security)", "url": "/v3/api-docs/identity-service"},
                {"name": "2. Master Service (Reference Data)", "url": "/v3/api-docs/master-service"},
                {"name": "3. User Service (Donors & Staff)", "url": "/v3/api-docs/user-service"},
                {"name": "4. Hospital Service (Hospitals)", "url": "/v3/api-docs/hospital-service"},
                {"name": "5. Blood Bank Service (Inventory)", "url": "/v3/api-docs/blood-bank-service"},
                {"name": "6. Donation Service (Donation Records)", "url": "/v3/api-docs/donation-service"},
                {"name": "7. Transaction Service (Requests & Issuance)", "url": "/v3/api-docs/transaction-service"},
                {"name": "8. Notification Service (Templates & Logs)", "url": "/v3/api-docs/notification-service"}
              ],
              "validatorUrl": ""
            }
            """;

    private static final String SWAGGER_INITIALIZER_JS = """
            window.onload = function() {
              window.ui = SwaggerUIBundle({
                configUrl: "/v3/api-docs/swagger-config",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                  SwaggerUIBundle.presets.apis,
                  SwaggerUIStandalonePreset
                ],
                plugins: [
                  SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
              });
            };
            """;

    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction() {
        return RouterFunctions.route()
                .GET("/swagger-ui.html", request ->
                        ServerResponse.status(HttpStatus.TEMPORARY_REDIRECT)
                                .location(URI.create("/webjars/swagger-ui/index.html"))
                                .build())
                .GET("/webjars/swagger-ui/swagger-initializer.js", request ->
                        ServerResponse.ok()
                                .header("Content-Type", "application/javascript")
                                .bodyValue(SWAGGER_INITIALIZER_JS))
                .GET("/v3/api-docs/swagger-config", request ->
                        ServerResponse.ok()
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .bodyValue(SWAGGER_CONFIG_JSON))
                .build();
    }
}
