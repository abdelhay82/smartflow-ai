package ma.smartflow.gatewayserver.filter;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import ma.smartflow.gatewayserver.services.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.config> {

    private final JwtService jwtService;

    @Override
    public GatewayFilter apply(config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if(!jwtService.isTokenValid(token)){
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtService.extractClaims(token);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.get("userId").toString())
                    .header("X-User-Username", claims.getSubject())
                    .header("X-User-Role", claims.get("role").toString())
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status){
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"error" : "%s}
                """.formatted(message);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }



    public static class config{

    }
}
