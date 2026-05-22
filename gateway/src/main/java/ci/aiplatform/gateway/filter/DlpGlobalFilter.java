package ci.aiplatform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DLP (Data Loss Prevention) Global Filter.
 * Scans body of POST/PUT requests for PII / secrets / API keys
 * and BLOCKS the request (HTTP 451) if detected.
 */
@Slf4j
@Component
public class DlpGlobalFilter implements GlobalFilter, Ordered {

    private record DlpRule(String name, Pattern pattern) {}

    private static final List<DlpRule> RULES = List.of(
        new DlpRule("EMAIL", Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")),
        new DlpRule("PHONE_CI", Pattern.compile("(?:\\+225|00225)?\\s?[0-9]{2}\\s?[0-9]{2}\\s?[0-9]{2}\\s?[0-9]{2}\\s?[0-9]{2}")),
        new DlpRule("IBAN", Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}")),
        new DlpRule("CNI_CI", Pattern.compile("CI[0-9]{9,12}")),
        new DlpRule("AWS_KEY", Pattern.compile("AKIA[0-9A-Z]{16}")),
        new DlpRule("PRIVATE_KEY", Pattern.compile("-----BEGIN [A-Z ]*PRIVATE KEY-----")),
        new DlpRule("PASSWORD_INLINE", Pattern.compile("(?i)(password|passwd|pwd)[\"':=\\s]+[^\\s\"',}]{6,}")),
        new DlpRule("BEARER_TOKEN", Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._\\-]{20,}"))
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String method = req.getMethod() != null ? req.getMethod().name() : "GET";

        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
            return chain.filter(exchange);
        }

        // Whitelist : ingestion documentaire (PDF/Word peuvent légitimement contenir téléphones/emails ;
        // l'anonymisation est faite côté IngestionService AVANT indexation Qdrant).
        String path = req.getURI().getPath();
        if (path.startsWith("/api/v1/documents/upload")) {
            return chain.filter(exchange);
        }
        // Skip également pour tout multipart (uploads de fichiers en général)
        String contentType = req.getHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return chain.filter(exchange);
        }

        return DataBufferUtils.join(req.getBody())
            .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
            .flatMap(buffer -> {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                DataBufferUtils.release(buffer);

                String body = new String(bytes, StandardCharsets.UTF_8);
                for (DlpRule rule : RULES) {
                    if (rule.pattern().matcher(body).find()) {
                        log.warn("DLP BLOCK rule={} path={} user={}",
                            rule.name(), req.getURI().getPath(),
                            exchange.getAttributeOrDefault("user_id", "?"));
                        exchange.getResponse().setStatusCode(HttpStatus.valueOf(451));
                        return exchange.getResponse().writeWith(Mono.just(
                            exchange.getResponse().bufferFactory()
                                .wrap(("{\"error\":\"DLP_BLOCKED\",\"rule\":\"" + rule.name() + "\"}")
                                    .getBytes(StandardCharsets.UTF_8))));
                    }
                }

                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(req) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                    }
                };
                return chain.filter(exchange.mutate().request(decorator).build());
            });
    }

    @Override public int getOrder() { return -1; }
}

