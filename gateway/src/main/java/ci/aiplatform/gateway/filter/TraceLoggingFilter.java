package ci.aiplatform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class TraceLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        exchange.getRequest().mutate().header("X-Trace-Id", traceId).build();

        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(t -> ((Jwt) t.getPrincipal()).getSubject())
            .defaultIfEmpty("anonymous")
            .flatMap(userId -> {
                MDC.put("trace_id", traceId);
                MDC.put("user_id", userId);
                return chain.filter(exchange).doFinally(s -> {
                    long latency = System.currentTimeMillis() - start;
                    log.info("req path={} status={} latency_ms={} user={} trace={}",
                        exchange.getRequest().getURI().getPath(),
                        exchange.getResponse().getStatusCode(),
                        latency, userId, traceId);
                    MDC.clear();
                });
            });
    }

    @Override public int getOrder() { return -10; }
}

