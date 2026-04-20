package com.sprintify.api_gateway.security;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtService jwtService;
    private final List<PathPattern> publicPaths;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;

        PathPatternParser parser = new PathPatternParser();

        this.publicPaths = List.of(
                parser.parse("/api/v1/auth/signup"),
                parser.parse("/api/v1/auth/login"),
            parser.parse("/identity-service/api/v1/auth/signup"),
            parser.parse("/identity-service/api/v1/auth/login"),
                parser.parse("/eureka/**")
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try{

            UUID userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            String email = jwtService.extractEmail(token);

            if (role == null || email == null || role.isBlank() || email.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Id", userId.toString())
                .header("X-User-Role", role)
                .header("X-User-Email", email)
                .build();

        return chain.filter(
                exchange.mutate()
                        .request(modifiedRequest)
                        .build()
        );
    } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        PathContainer container = PathContainer.parsePath(path);

        return publicPaths.stream()
                .anyMatch(pattern -> pattern.matches(container));
    }
}