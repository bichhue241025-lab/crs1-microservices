package com.example.api_gateway.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8094")
                .build();
    }

    public Mono<Boolean> isValidForScope(
            String key,
            String scope
    ) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/api-keys/validate")
                        .queryParam("key", key)
                        .queryParam("scope", scope)
                        .build()
                )
                .retrieve()
                .bodyToMono(Map.class)
                .map(res ->
                        Boolean.TRUE.equals(
                                res.get("valid")
                        )
                )

                // Neu auth-service loi / khong ket noi duoc
                // thi coi API Key la khong hop le
                .onErrorReturn(false);
    }
}