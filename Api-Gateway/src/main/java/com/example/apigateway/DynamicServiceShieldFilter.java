package com.example.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class DynamicServiceShieldFilter implements GlobalFilter, Ordered {

    private final DatabaseClient databaseClient;

    // Map URI path prefixes to registered database service names
    private static final Map<String, String> PATH_TO_SERVICE_MAP = Map.of(
            "/employee", "EMPLOYEE-SERVICE",
            "/address", "ADDRESS-SERVICE",
            "/email", "EMAIL-SERVICE"
    );

    public DynamicServiceShieldFilter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Identify targeted microservice based on request path
        String targetService = resolveServiceName(path);

        // Pass through if the route is unmonitored or public
        if (targetService == null) {
            return chain.filter(exchange);
        }

        // 2. Non-blocking R2DBC check in service_circuit_state for this specific service
        return databaseClient.sql("SELECT status FROM service_circuit_state WHERE service_name = :serviceName")
                .bind("serviceName", targetService)
                .map((row, metadata) -> row.get("status", String.class))
                .first()
                .defaultIfEmpty("NORMAL")
                .flatMap(status -> {
                    if ("SHIELDED".equalsIgnoreCase(status)) {
                        // 3. Block requests ONLY for this particular service
                        return rejectRequest(exchange, targetService);
                    }
                    // 4. Forward requests if service status is NORMAL
                    return chain.filter(exchange);
                });
    }

    private String resolveServiceName(String path) {
        for (Map.Entry<String, String> entry : PATH_TO_SERVICE_MAP.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Mono<Void> rejectRequest(ServerWebExchange exchange, String serviceName) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format("""
                {
                    "status": "ERROR",
                    "service": "%s",
                    "message": "Please try again , application is under service :"
                }
                """, serviceName);

        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(responseBytes))
        );
    }

    @Override
    public int getOrder() {
        return -1; // High priority to intercept before routing
    }
}