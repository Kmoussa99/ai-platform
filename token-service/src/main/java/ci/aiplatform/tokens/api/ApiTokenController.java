package ci.aiplatform.tokens.api;

import ci.aiplatform.tokens.domain.ApiToken;
import ci.aiplatform.tokens.domain.Scope;
import ci.aiplatform.tokens.service.ApiTokenService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class ApiTokenController {

    private final ApiTokenService service;

    public record CreateRequest(@NotBlank String name, Set<Scope> scopes,
                                 Integer rateLimit, LocalDateTime expiresAt,
                                 List<String> allowedIps) {}

    public record TokenView(UUID id, String name, String prefix, Set<Scope> scopes,
                             Integer rateLimit, LocalDateTime expiresAt, boolean active,
                             LocalDateTime lastUsedAt, long usageCount, LocalDateTime createdAt) {
        public static TokenView from(ApiToken t) {
            return new TokenView(t.getId(), t.getName(), t.getPrefix(), t.getScopes(),
                t.getRateLimit(), t.getExpiresAt(), t.isActive(),
                t.getLastUsedAt(), t.getUsageCount(), t.getCreatedAt());
        }
    }

    public record CreateResponse(TokenView token, String secret) {}

    @PostMapping
    public CreateResponse create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateRequest req) {
        var c = service.create(jwt.getSubject(), req.name(), req.scopes(),
            req.rateLimit(), req.expiresAt(), req.allowedIps());
        return new CreateResponse(TokenView.from(c.meta()), c.plaintext());
    }

    @GetMapping
    public List<TokenView> list(@AuthenticationPrincipal Jwt jwt) {
        return service.listMine(jwt.getSubject()).stream().map(TokenView::from).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.revoke(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rotate")
    public CreateResponse rotate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        var c = service.rotate(id, jwt.getSubject());
        return new CreateResponse(TokenView.from(c.meta()), c.plaintext());
    }

    @GetMapping("/{id}/usage")
    public Map<String, Object> usage(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        // Stats simplifiées : usage_count + last_used_at (graphes détaillés via Prometheus/Loki)
        return service.listMine(jwt.getSubject()).stream()
            .filter(t -> t.getId().equals(id)).findFirst()
            .map(t -> Map.<String, Object>of("usage_count", t.getUsageCount(),
                "last_used_at", t.getLastUsedAt()))
            .orElseThrow();
    }
}

