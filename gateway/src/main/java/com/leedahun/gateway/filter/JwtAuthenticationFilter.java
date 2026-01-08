package com.leedahun.gateway.filter;

import static com.leedahun.gateway.common.message.ErrorMessage.TOKEN_EXPIRED;
import static com.leedahun.gateway.common.message.ErrorMessage.TOKEN_INVALID;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.gateway.common.response.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory {

    @Autowired
    private ObjectMapper objectMapper;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String JWT_PREFIX = "Bearer ";
    private static final String ID_CLAIM = "id";

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // Request Header 토큰 가져오기
            String token = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // 토큰이 없을 경우 401
            if (token == null || !token.startsWith(JWT_PREFIX)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Long userId = null;
            String role = null;
            try {
                DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(jwtSecret))
                        .build()
                        .verify(token.replace(JWT_PREFIX, ""));
                userId = decodedJWT.getClaim(ID_CLAIM).asLong();
                role = decodedJWT.getClaim("role").asString();
            } catch (TokenExpiredException e) {
                log.warn("토큰이 만료되었습니다.", e);
                return onError(exchange, TOKEN_EXPIRED.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (JWTVerificationException e) {
                log.warn("토큰이 유효하지 않습니다.", e);
                return onError(exchange, TOKEN_INVALID.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            // userId를 X-User-Id 헤더에 담아서 다른 마이크로서비스에 전달
            return chain.filter(
                    exchange.mutate()
                            .request(
                                    exchange.getRequest()
                                            .mutate()
                                            .header("X-User-Id", String.valueOf(userId))
                                            .header("X-User-Roles", role)
                                            .build()
                            )
                            .build()
            );
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpResponse httpResponse = new HttpResponse(status, message, null);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(httpResponse);

            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 중 오류 발생", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }
}