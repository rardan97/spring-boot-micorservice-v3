package com.blackcode.api_gateway.filter;

import com.blackcode.api_gateway.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtUtils jwtUtils;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        System.out.println(path.toString());
        if (path.startsWith("/api/auth/signup") || path.startsWith("/api/auth/signin") || path.startsWith("/api/auth/v3/api-docs")) {
            System.out.println("===================================");
            System.out.println("Prossess signup check : " + path.startsWith("/api/auth/signup"));
            System.out.println("Prossess signup");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return this.onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtils.getUserNameFromJwtToken(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.add("X-Username", username);
                }))
                .build();

        return chain.filter(mutatedExchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }



}
