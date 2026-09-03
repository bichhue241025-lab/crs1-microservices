package com.example.api_gateway.filter;

import com.example.api_gateway.cache.ApiKeyValidationCache;
import com.example.api_gateway.client.AuthServiceClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;
    private final ApiKeyValidationCache cache;

    // Route doi tac can bao ve
    private static final String PARTNER_PATH =
            "/api/public/courses";

    // Scope bat buoc cua route nay
    private static final String REQUIRED_SCOPE =
            "courses:read";

    public ApiKeyFilter(
            AuthServiceClient authServiceClient,
            ApiKeyValidationCache cache
    ) {
        this.authServiceClient = authServiceClient;
        this.cache = cache;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        // Khong phai route doi tac
        // -> bo qua ApiKeyFilter
        if (!path.startsWith(PARTNER_PATH)) {
            return chain.filter(exchange);
        }

        // Lay API Key tu header
        String apiKey =
                request.getHeaders()
                        .getFirst("X-API-KEY");

        // Khong co key
        if (apiKey == null || apiKey.isBlank()) {
            return reject(exchange);
        }

        // Cache phai gom ca key + scope
        String cacheKey =
                apiKey + ":" + REQUIRED_SCOPE;

        Boolean cached = cache.get(cacheKey);

        // Da co ket qua trong cache
        if (cached != null) {

            if (cached) {
                return chain.filter(exchange);
            }

            return reject(exchange);
        }

        // Chua co cache
        // -> goi auth-service kiem tra
        return authServiceClient
                .isValidForScope(
                        apiKey,
                        REQUIRED_SCOPE
                )
                .flatMap(valid -> {

                    // Luu ket qua 30 giay
                    cache.put(
                            cacheKey,
                            valid
                    );

                    if (valid) {
                        return chain.filter(exchange);
                    }

                    return reject(exchange);
                });
    }

    private Mono<Void> reject(
            ServerWebExchange exchange
    ) {

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.FORBIDDEN
                );

        return exchange.getResponse()
                .setComplete();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}